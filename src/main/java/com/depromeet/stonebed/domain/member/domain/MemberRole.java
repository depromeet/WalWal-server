package com.depromeet.stonebed.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberRole {
    USER("ROLE_USER"),
    ADMIN("ROLE_ADMIN"),
    TEMPORARY("ROLE_TEMPORARY"),
    ;

    private final String value;
}
