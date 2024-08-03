package com.depromeet.stonebed.domain.auth.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AppleTokenRequest(
        // 외부 통신 시 snake_case로 요청 및 응답
        @JsonProperty("code") String code,
        @JsonProperty("client_id") String clientId,
        @JsonProperty("client_secret") String clientSecret,
        @JsonProperty("grant_type") String grantType) {
    public static AppleTokenRequest of(
            String code, String clientId, String clientSecret, String grantType) {
        return new AppleTokenRequest(code, clientId, clientSecret, grantType);
    }
}
