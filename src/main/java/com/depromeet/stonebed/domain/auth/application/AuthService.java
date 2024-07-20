package com.depromeet.stonebed.domain.auth.application;

import com.depromeet.stonebed.domain.auth.application.apple.AppleClient;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.domain.TokenType;
import com.depromeet.stonebed.domain.auth.dto.RefreshTokenDto;
import com.depromeet.stonebed.domain.auth.dto.request.RefreshTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.response.AuthTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.application.MemberService;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.domain.member.dto.request.CreateMemberRequest;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
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
    private final JwtTokenService jwtTokenService;
    private final MemberUtil memberUtil;

    public SocialClientResponse authenticateFromProvider(OAuthProvider provider, String token) {
        /* token
        1. apple의 경우 authorizationCode Value
        2. kakao의 경우 accessToken Value
         */
        return switch (provider) {
            case APPLE -> appleClient.authenticateFromApple(token);
                // TODO: 추후 카카오 개발 예정
                // case KAKAO -> authenticateFromKakao(accessToken);
            default -> throw new InvalidParameterException();
        };
    }

    @Transactional
    public AuthTokenResponse socialLogin(
            OAuthProvider oAuthProvider, String oauthId, String email) {
        Optional<Member> memberOptional = memberService.getMemberByOauthId(oAuthProvider, oauthId);

        if (memberOptional.isEmpty()) {
            // 회원가입이 안된 경우, 회원가입 진행
            Member newMember = memberService.socialSignUp(oAuthProvider, oauthId, email);
            // 임시 토큰 발행
            TokenPairResponse temporaryTokenPair =
                    jwtTokenService.generateTemporaryTokenPair(
                            oAuthProvider, newMember.getOauthInfo().getOauthId());
            newMember.updateLastLoginAt();

            return AuthTokenResponse.of(temporaryTokenPair, true);
        } else {
            Member member = memberOptional.get();
            // 사용자 로그인 토큰 생성
            TokenPairResponse tokenPair = getLoginResponse(member);
            member.updateLastLoginAt();

            return AuthTokenResponse.of(tokenPair, false);
        }
    }

    public AuthTokenResponse registerMember(CreateMemberRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        // 사용자 회원가입
        if (memberUtil.getMemberTokenType() == TokenType.TEMPORARY) {
            OAuthProvider oAuthProvider = OAuthProvider.from(memberUtil.getMemberProvider());

            Member member =
                    memberService.getOrCreateMember(
                            oAuthProvider, currentMember.getOauthInfo().getOauthId(), request);
            // 새 토큰 생성
            TokenPairResponse tokenPair = getLoginResponse(member);
            return AuthTokenResponse.of(tokenPair, false);
        }
        throw new CustomException(ErrorCode.AUTHORIZATION_FAILED);
    }

    @Transactional(readOnly = true)
    public AuthTokenResponse reissueTokenPair(RefreshTokenRequest request) {
        // 리프레시 토큰을 이용해 새로운 액세스 토큰 발급
        RefreshTokenDto refreshTokenDto =
                jwtTokenService.retrieveRefreshToken(request.refreshToken());
        RefreshTokenDto refreshToken =
                jwtTokenService.createRefreshTokenDto(refreshTokenDto.memberId());

        Member member = memberUtil.getMemberByMemberId(refreshToken.memberId());

        TokenPairResponse tokenPair = getLoginResponse(member);
        return AuthTokenResponse.of(tokenPair, false);
    }

    private TokenPairResponse getLoginResponse(Member member) {
        return jwtTokenService.generateTokenPair(member.getId(), MemberRole.USER);
    }
}
