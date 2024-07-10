package com.depromeet.stonebed.domain.auth.application;

import com.depromeet.stonebed.domain.auth.application.apple.AppleClient;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.response.IdentifierResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialLoginResponse;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.application.MemberService;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
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

    public IdentifierResponse authenticateFromProvider(OAuthProvider provider, String accessToken) {
        return switch (provider) {
            case APPLE -> appleClient.authenticateFromApple(accessToken);
                // TODO: 추후 카카오 개발 예정
                // case KAKAO -> authenticateFromKakao(accessToken);
            default -> throw new InvalidParameterException();
        };
    }

    @Transactional
    public SocialLoginResponse socialLogin(OAuthProvider oAuthProvider, String identifier) {
        // 위 결과에서 나온 identifier(sub)로 이미 있는 사용자인지 확인
        Optional<Member> memberByOauthId =
                memberService.getMemberByOauthId(oAuthProvider, identifier);
        Member member = memberByOauthId.orElseGet(() -> signUp(oAuthProvider, identifier));

        member.updateLastLoginAt();

        // if (member.isEmpty()) {
        // 	//회원가입이 안된 경우 임시 토큰 발행
        // 	TokenPairResponse temporaryTokenPair = tokenGenerator
        // 		.generateTemporaryTokenPair(socialLoginProvider, IdentifierResponse.identifier());
        // 	return SocialLoginResponse.of(temporaryTokenPair, true);
        // }

        // 새 토큰 생성
        TokenPairResponse tokenPair = getLoginResponse(member);
        return SocialLoginResponse.of(member, tokenPair);
    }

    private TokenPairResponse getLoginResponse(Member member) {
        String accessToken = jwtTokenService.createAccessToken(member.getId(), member.getRole());
        String refreshToken = jwtTokenService.createRefreshToken(member.getId());

        return TokenPairResponse.from(accessToken, refreshToken);
    }

    private Member signUp(OAuthProvider oAuthProvider, String identifier) {
        Member member = createMember(oAuthProvider, identifier);
        return memberRepository.save(member);
    }

    private Member createMember(OAuthProvider oAuthProvider, String identifier) {
        return null;
    }
}
