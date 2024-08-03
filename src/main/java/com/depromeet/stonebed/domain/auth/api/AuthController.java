package com.depromeet.stonebed.domain.auth.api;

import com.depromeet.stonebed.domain.auth.application.AuthService;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.request.RefreshTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.request.SocialLoginRequest;
import com.depromeet.stonebed.domain.auth.dto.response.AuthTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.domain.member.dto.request.CreateMemberRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1-1. [인증]", description = "인증 관련 API입니다.")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "소셜 로그인", description = "소셜 로그인 후 임시 토큰을 발급합니다.")
    @PostMapping("/social-login/{provider}")
    public AuthTokenResponse socialLogin(
            @PathVariable(name = "provider")
                    @Parameter(example = "apple", description = "OAuth 제공자")
                    String provider,
            @RequestBody @Valid SocialLoginRequest request) {
        OAuthProvider oAuthProvider = OAuthProvider.from(provider);

        SocialClientResponse socialClientResponse =
                authService.authenticateFromProvider(oAuthProvider, request.token());

        // 위 결과에서 나온 oauthId와 email로 토큰 발급
        return authService.socialLogin(
                oAuthProvider, socialClientResponse.oauthId(), socialClientResponse.email());
    }

    @Operation(summary = "회원가입", description = "회원가입을 진행 후 토큰 발급")
    @PostMapping("/register")
    public AuthTokenResponse register(@RequestBody @Valid CreateMemberRequest request) {
        return authService.registerMember(request);
    }

    @Operation(summary = "리프레시 토큰 발급", description = "리프레시 토큰을 이용해 새로운 액세스 토큰을 발급합니다.")
    @PostMapping("/reissue")
    public AuthTokenResponse reissueTokenPair(@RequestBody @Valid RefreshTokenRequest request) {
        return authService.reissueTokenPair(request);
    }

    @Operation(summary = "회원탈퇴 기능", description = "회원탈퇴를 진행합니다.")
    @PostMapping("/withdraw")
    public ResponseEntity<Void> withdraw() {
        authService.withdraw();
        return ResponseEntity.ok().build();
    }
}
