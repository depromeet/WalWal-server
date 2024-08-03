package com.depromeet.stonebed.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record SocialLoginRequest(
        @Schema(description = "apple auth code", example = "authorization_code") String token) {}
