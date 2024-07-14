package com.depromeet.stonebed.domain.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RaisePet {
    DOG("강아지"),
    CAT("고양이"),
    ;

    private final String value;
}
