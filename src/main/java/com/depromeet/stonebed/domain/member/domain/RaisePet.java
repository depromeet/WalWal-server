package com.depromeet.stonebed.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RaisePet {
    DOG("DOG"),
    CAT("CAT"),
    ;

    private final String value;
}
