package com.depromeet.stonebed.domain.missionRecord.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionRecordDisplay {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    FORBIDDEN("FORBIDDEN"),
    ;
    private final String value;
}
