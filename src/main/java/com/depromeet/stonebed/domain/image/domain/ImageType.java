package com.depromeet.stonebed.domain.image.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImageType {
    MISSION("mission"),
    MISSION_RECORD("mission_record"),
    MEMBER_PROFILE("member_profile"),
    ;
    private final String value;
}
