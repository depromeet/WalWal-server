package com.depromeet.stonebed.domain.auth.api;

import com.depromeet.stonebed.domain.auth.application.AuthService;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.request.IdTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.response.SocialLoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1-1. [인증]", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "소셜 로그인(애플)", description = "소셜 로그인 후 토큰을 발급합니다.")
    @PostMapping("/social-login/apple")
    public ResponseEntity<SocialLoginResponse> socialLogin(
            @RequestParam(name = "provider") OAuthProvider provider,
            @Valid @RequestBody IdTokenRequest request) {

        return ResponseEntity.ok(SocialLoginResponse.of(null, null));
    }
}
