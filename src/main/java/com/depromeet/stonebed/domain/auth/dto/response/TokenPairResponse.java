package com.depromeet.stonebed.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record TokenPairResponse(
        @Schema(description = "액세스 토큰", defaultValue = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", defaultValue = "refreshToken") String refreshToken) {

    public static TokenPairResponse of(String accessToken, String refreshToken) {
        return new TokenPairResponse(accessToken, refreshToken);
    }
}
