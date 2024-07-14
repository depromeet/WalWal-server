package com.depromeet.stonebed.domain.auth.domain;

import java.security.InvalidParameterException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum TokenType {
    ACCESS("access"),
    REFRESH("refresh"),
    TEMPORARY("temporary");
    private final String typeKey;

    public static TokenType from(String typeKey) {
        return switch (typeKey.toUpperCase()) {
            case "ACCESS" -> ACCESS;
            case "REFRESH" -> REFRESH;
            case "TEMPORARY" -> TEMPORARY;
            default -> throw new InvalidParameterException();
        };
    }
}
