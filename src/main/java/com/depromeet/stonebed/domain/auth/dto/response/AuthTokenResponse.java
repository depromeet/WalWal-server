package com.depromeet.stonebed.domain.auth.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record AuthTokenResponse(
        @Schema(description = "엑세스 토큰", example = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", example = "refreshToken") String refreshToken,
        @Schema(description = "임시 토큰 여부", example = "false") boolean isTemporaryToken) {
    public static AuthTokenResponse of(
            TokenPairResponse tokenPairResponse, boolean isTemporaryToken) {
        return new AuthTokenResponse(
                tokenPairResponse.accessToken(),
                tokenPairResponse.refreshToken(),
                isTemporaryToken);
    }
}
