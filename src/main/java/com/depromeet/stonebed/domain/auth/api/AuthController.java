package com.depromeet.stonebed.domain.auth.api;

import com.depromeet.stonebed.domain.auth.application.AuthService;
import com.depromeet.stonebed.domain.auth.application.JwtTokenService;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.domain.TokenType;
import com.depromeet.stonebed.domain.auth.dto.AuthenticationToken;
import com.depromeet.stonebed.domain.auth.dto.RefreshTokenDto;
import com.depromeet.stonebed.domain.auth.dto.request.RefreshTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.request.SocialLoginRequest;
import com.depromeet.stonebed.domain.auth.dto.response.AuthTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.application.MemberService;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.domain.member.dto.CreateNewUserDTO;
import com.depromeet.stonebed.domain.member.dto.request.CreateMemberRequest;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1-1. [인증]", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final JwtTokenService jwtTokenService;

    @Operation(summary = "소셜 로그인", description = "소셜 로그인 후 임시 토큰을 발급합니다.")
    @PostMapping("/social-login/{provider}")
    public AuthTokenResponse socialLogin(
            @PathVariable(name = "provider")
                    @Parameter(example = "apple", description = "OAuth 제공자")
                    String provider,
            @RequestBody @Valid SocialLoginRequest request)
            throws IOException {
        OAuthProvider oAuthProvider = OAuthProvider.fromString(provider);

        SocialClientResponse socialClientResponse =
                authService.authenticateFromProvider(oAuthProvider, request.token());

        // 위 결과에서 나온 identifier로 이미 있는 사용자인지 확인
        Optional<Member> member =
                memberService.getMemberByOauthId(oAuthProvider, socialClientResponse.oauthId());
        if (member.isEmpty()) {
            // 회원가입이 안된 경우 임시 토큰 발행
            TokenPairResponse temporaryTokenPair =
                    jwtTokenService.generateTemporaryTokenPair(
                            oAuthProvider, socialClientResponse.oauthId());
            return AuthTokenResponse.of(temporaryTokenPair, true);
        }

        // 사용자로 토큰 생성
        TokenPairResponse tokenPair =
                jwtTokenService.generateTokenPair(member.get().getId(), MemberRole.USER);
        return AuthTokenResponse.of(tokenPair, false);
    }

    @Operation(summary = "회원가입", description = "회원가입을 진행 후 토큰 발급")
    @PostMapping("/register")
    public AuthTokenResponse register(Authentication authentication, CreateMemberRequest request) {
        // 사용자 회원가입
        if (authentication.getCredentials() instanceof AuthenticationToken token
                && token.tokenType() == TokenType.TEMPORARY) {
            OAuthProvider oAuthProvider = OAuthProvider.fromString(token.provider());

            // oauthId로 이미 있는 사용자인지 확인
            Optional<Member> preExistsMember =
                    memberService.getMemberByOauthId(oAuthProvider, token.userId());
            if (preExistsMember.isPresent()) {
                throw new CustomException(ErrorCode.ALREADY_EXISTS_MEMBER);
            }

            CreateNewUserDTO createNewUserDTO =
                    CreateNewUserDTO.of(
                            oAuthProvider,
                            token.userId(),
                            request.nickname(),
                            request.raisePet(),
                            request.profileImageUrl(),
                            request.email());
            Member member = memberService.createNewMember(createNewUserDTO);

            // 새 토큰 생성
            TokenPairResponse tokenPair =
                    jwtTokenService.generateTokenPair(member.getId(), MemberRole.USER);
            return AuthTokenResponse.of(tokenPair, false);
        }
        // 일어날 수 없는 일
        throw new CustomException(ErrorCode.AUTHORIZATION_FAILED);
    }

    @Operation(summary = "리프레시 토큰 발급", description = "리프레시 토큰을 이용해 새로운 액세스 토큰을 발급합니다.")
    @PostMapping("/reissue")
    public AuthTokenResponse reissueTokenPair(RefreshTokenRequest request) {
        // 리프레시 토큰을 이용해 새로운 액세스 토큰 발급
        RefreshTokenDto refreshTokenDto =
                jwtTokenService.retrieveRefreshToken(request.refreshToken());
        RefreshTokenDto refreshToken =
                jwtTokenService.createRefreshTokenDto(refreshTokenDto.memberId());

        TokenPairResponse tokenPair =
                jwtTokenService.generateTokenPair(refreshToken.memberId(), MemberRole.USER);
        return AuthTokenResponse.of(tokenPair, false);
    }
}
