package com.depromeet.stonebed.domain.auth.domain;

import java.security.InvalidParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {
    KAKAO("KAKAO"),
    APPLE("APPLE"),
    ;
    private final String value;

    public static OAuthProvider from(String provider) {
        return switch (provider.toUpperCase()) {
            case "APPLE" -> APPLE;
            case "KAKAO" -> KAKAO;
            default -> throw new InvalidParameterException();
        };
    }
}
