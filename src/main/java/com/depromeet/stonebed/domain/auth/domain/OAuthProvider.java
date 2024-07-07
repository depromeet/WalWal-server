package com.depromeet.stonebed.domain.auth.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {
    KAKAO("KAKAO"),
    APPLE("APPLE"),
    ;
    private final String value;
}
