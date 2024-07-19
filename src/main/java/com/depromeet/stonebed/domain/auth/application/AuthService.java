package com.depromeet.stonebed.domain.auth.application;

import com.depromeet.stonebed.domain.auth.application.apple.AppleClient;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.response.AuthTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.application.MemberService;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.depromeet.stonebed.domain.member.domain.OauthInfo;
import com.depromeet.stonebed.domain.member.domain.Profile;
import java.security.InvalidParameterException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppleClient appleClient;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final JwtTokenService jwtTokenService;

    public SocialClientResponse authenticateFromProvider(
            OAuthProvider provider, String accessToken) {
        return switch (provider) {
            case APPLE -> appleClient.authenticateFromApple(accessToken);
                // TODO: 추후 카카오 개발 예정
                // case KAKAO -> authenticateFromKakao(accessToken);
            default -> throw new InvalidParameterException();
        };
    }

    @Transactional
    public AuthTokenResponse socialLogin(
            OAuthProvider oAuthProvider, String oauthId, String email) {
        // 위 결과에서 나온 identifier(sub)로 이미 있는 사용자인지 확인
        Optional<Member> memberOptional = memberService.getMemberByOauthId(oAuthProvider, oauthId);

        if (memberOptional.isEmpty()) {
            // 회원가입이 안된 경우, 회원가입 진행
            Member newMember = signUp(oAuthProvider, oauthId, email);
            // 임시 토큰 발행
            TokenPairResponse temporaryTokenPair =
                    jwtTokenService.generateTemporaryTokenPair(
                            oAuthProvider, newMember.getOauthInfo().getOauthId());
            return AuthTokenResponse.of(temporaryTokenPair, true);
        } else {
            Member member = memberOptional.get();
            // member가 존재할 시 최근 로그인 시간 변경
            member.updateLastLoginAt();

            // 사용자로 토큰 생성
            TokenPairResponse tokenPair = getLoginResponse(member);
            return AuthTokenResponse.of(tokenPair, false);
        }
    }

    public TokenPairResponse getLoginResponse(Member member) {
        String accessToken = jwtTokenService.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenService.createRefreshToken(member.getId());

        member.updateLastLoginAt();

        return TokenPairResponse.of(accessToken, refreshToken);
    }

    private Member signUp(OAuthProvider oAuthProvider, String oauthId, String email) {
        Member member = createMember(oAuthProvider, oauthId, email);
        return memberRepository.save(member);
    }

    private Member createMember(OAuthProvider oAuthProvider, String oauthId, String email) {
        OauthInfo oauthInfo = OauthInfo.createOauthInfo(oauthId, oAuthProvider.getValue(), email);

        return Member.createMember(
                Profile.createProfile("", ""),
                oauthInfo,
                MemberStatus.NORMAL,
                MemberRole.USER,
                null);
    }
}
