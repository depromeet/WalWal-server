package com.depromeet.stonebed.domain.auth.dto.request;

public record AppleTokenRequest(
        // 외부 통신 시 snake_case로 요청 및 응답
        String code,
        String client_id,
        String client_secret,
        String grant_type,
        String refresh_token) {
    public static AppleTokenRequest of(
            String code,
            String clientId,
            String clientSecret,
            String grantType,
            String refreshToken) {
        return new AppleTokenRequest(code, clientId, clientSecret, grantType, refreshToken);
    }
}
