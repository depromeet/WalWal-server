package com.depromeet.stonebed.domain.auth.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleTokenResponse(
        // 외부 통신 시 snake_case로 요청 및 응답
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in") Long expiresIn,
        @JsonProperty("id_token") String idToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("token_type") String tokenType) {
    public static AppleTokenResponse of(
            String accessToken,
            Long expiresIn,
            String idToken,
            String refreshToken,
            String tokenType) {
        return new AppleTokenResponse(accessToken, expiresIn, idToken, refreshToken, tokenType);
    }
}
