package com.depromeet.stonebed.domain.auth.api;

import com.depromeet.stonebed.domain.auth.application.AuthService;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.request.IdTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.response.IdentifierResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialLoginResponse;
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

@Tag(name = "1-1. [인증]", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "소셜 로그인", description = "소셜 로그인 후 토큰을 발급합니다.")
    @PostMapping("/social-login/{provider}")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @PathVariable(name = "provider")
                    @Parameter(example = "apple", description = "OAuth 제공자")
                    String provider,
            @RequestBody @Valid IdTokenRequest request) {
        OAuthProvider oAuthProvider = OAuthProvider.fromString(provider);

        IdentifierResponse identifierResponse =
                authService.authenticateFromProvider(oAuthProvider, request.idToken());

        return ResponseEntity.ok(
                authService.socialLogin(oAuthProvider, identifierResponse.identifier()));
    }
}
