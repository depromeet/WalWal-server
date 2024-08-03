package com.depromeet.stonebed.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record RefreshTokenRequest(
        @Schema(description = "리프레시 토큰", example = "refreshToken") String refreshToken) {}
