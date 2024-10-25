package com.depromeet.stonebed.domain.auth.application;

import com.depromeet.stonebed.domain.auth.application.apple.AppleClient;
import com.depromeet.stonebed.domain.auth.application.kakao.KakaoClient;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.RefreshTokenDto;
import com.depromeet.stonebed.domain.auth.dto.request.RefreshTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.response.AuthTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.comment.dao.CommentRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmNotificationRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmTokenRepository;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.member.dto.request.CreateMemberRequest;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.security.JwtTokenProvider;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final FcmNotificationRepository fcmNotificationRepository;
    private final MemberRepository memberRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MissionRecordBoostRepository missionRecordBoostRepository;
    private final FcmTokenRepository fcmTokenRepository;
    private final CommentRepository commentRepository;

    private final AppleClient appleClient;
    private final KakaoClient kakaoClient;
    private final JwtTokenProvider jwtTokenProvider;
    private final MemberUtil memberUtil;

    public SocialClientResponse authenticateFromProvider(OAuthProvider provider, String token) {
        /* token
        1. apple의 경우 authorizationCode Value
        2. kakao의 경우 accessToken Value
         */
        return switch (provider) {
            case APPLE -> appleClient.authenticateFromApple(token);
            case KAKAO -> kakaoClient.authenticateFromKakao(token);
        };
    }

    public AuthTokenResponse socialLogin(
            OAuthProvider oAuthProvider, String oauthId, String email) {
        Optional<Member> memberOptional =
                memberRepository.findByOauthInfoOauthProviderAndOauthInfoOauthId(
                        oAuthProvider.getValue(), oauthId);

        return memberOptional
                .map(
                        member -> {
                            // 사용자 로그인 토큰 생성
                            TokenPairResponse tokenPair =
                                    member.getRole() == MemberRole.TEMPORARY
                                            ? getTemporaryLoginResponse(member)
                                            : getLoginResponse(member);
                            member.updateLastLoginAt();
                            updateMemberNormalStatus(member);
                            log.info("소셜 로그인 진행: {}", member.getId());
                            return AuthTokenResponse.of(
                                    tokenPair, member.getRole() == MemberRole.TEMPORARY);
                        })
                .orElseGet(
                        () -> {
                            // 회원가입이 안된 경우, 임시 회원가입 진행
                            Member newMember =
                                    Member.createOAuthMember(oAuthProvider, oauthId, email);
                            memberRepository.save(newMember);

                            // 임시 토큰 발행
                            TokenPairResponse temporaryTokenPair =
                                    jwtTokenProvider.generateTemporaryTokenPair(newMember);
                            newMember.updateLastLoginAt();
                            log.info("임시 회원가입 진행: {}", newMember.getId());
                            return AuthTokenResponse.of(temporaryTokenPair, true);
                        });
    }

    // 회원가입
    public AuthTokenResponse registerMember(CreateMemberRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        // 사용자 회원가입
        if (memberUtil.getMemberRole().equals(MemberRole.TEMPORARY.getValue())) {
            // 명시적 변경 감지
            Member registerMember = registerMember(currentMember, request);

            // 새 토큰 생성
            TokenPairResponse tokenPair = getLoginResponse(registerMember);
            log.info("일반 회원가입 진행: {}", registerMember.getId());
            return AuthTokenResponse.of(tokenPair, false);
        }
        throw new CustomException(ErrorCode.ALREADY_EXISTS_MEMBER);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse reissueTokenPair(RefreshTokenRequest request) {
        // 리프레시 토큰을 이용해 새로운 액세스 토큰 발급
        RefreshTokenDto refreshTokenDto =
                jwtTokenProvider.retrieveRefreshToken(request.refreshToken());
        RefreshTokenDto refreshToken =
                jwtTokenProvider.createRefreshTokenDto(refreshTokenDto.memberId());

        Member member = memberUtil.getMemberByMemberId(refreshToken.memberId());

        TokenPairResponse tokenPair = getLoginResponse(member);
        return AuthTokenResponse.of(tokenPair, false);
    }

    private TokenPairResponse getLoginResponse(Member member) {
        return jwtTokenProvider.generateTokenPair(member.getId(), MemberRole.USER);
    }

    private TokenPairResponse getTemporaryLoginResponse(Member member) {
        return jwtTokenProvider.generateTokenPair(member.getId(), MemberRole.TEMPORARY);
    }

    public void withdraw() {
        Member member = memberUtil.getCurrentMember();
        /**
         * TODO: 런칭데이 이후 고도화 if (provider.equals(OAuthProvider.APPLE)) {
         * appleClient.withdraw(member.getOauthInfo().getOauthId()); }
         */
        validateMemberStatusDelete(member.getStatus());

        List<MissionRecord> missionRecords = missionRecordRepository.findAllByMember(member);
        member.updateMemberRole(MemberRole.TEMPORARY);
        member.updateProfile(Profile.createProfile("", ""));
        member.updateOauthId("");
        memberRepository.flush();
        withdrawMemberRelationByMemberId(
                missionRecords.stream().map(MissionRecord::getId).toList(), member.getId());

        jwtTokenProvider.deleteRefreshToken(member.getId());

        memberRepository.deleteById(member.getId());
    }

    private void validateMemberStatusDelete(MemberStatus status) {
        if (status == MemberStatus.DELETED) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_DELETED);
        }
    }

    private Member registerMember(Member member, CreateMemberRequest request) {
        member.updateProfile(
                Profile.createProfile(
                        request.nickname(),
                        request.profileImageUrl() == null
                                ? member.getProfile().getProfileImageUrl()
                                : request.profileImageUrl()));
        member.updateRaisePet(request.raisePet());
        member.updateMemberRole(MemberRole.USER);
        memberRepository.save(member);
        return member;
    }

    private void updateMemberNormalStatus(Member member) {
        if (member.getStatus() == MemberStatus.DELETED) {
            member.updateStatus(MemberStatus.NORMAL);
        }
    }

    private void withdrawMemberRelationByMemberId(List<Long> recordIds, Long memberId) {
        missionRecordBoostRepository.deleteAllByRecordIds(recordIds);
        missionRecordRepository.deleteAllByMember(memberId);
        fcmNotificationRepository.deleteAllByMember(memberId);
        fcmTokenRepository.deleteAllByMember(memberId);
    }
}
