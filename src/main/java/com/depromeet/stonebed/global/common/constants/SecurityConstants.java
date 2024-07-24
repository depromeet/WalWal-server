package com.depromeet.stonebed.global.common.constants;

public final class SecurityConstants {

    public static final String TOKEN_ROLE_NAME = "role";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String APPLE_JWK_SET_URL = "https://appleid.apple.com/auth/keys";
    public static final String APPLE_ISSUER = "https://appleid.apple.com";
    public static final String APPLE_TOKEN_URL = "https://appleid.apple.com/auth/token";
    public static final String APPLE_GRANT_TYPE = "authorization_code";
    public static final String ACCESS_TOKEN_COOKIE_NAME = "accessToken";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    public static final String APPLICATION_URLENCODED = "application/x-www-form-urlencoded";

    private SecurityConstants() {}
}
