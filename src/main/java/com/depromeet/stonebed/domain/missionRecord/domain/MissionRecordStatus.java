package com.depromeet.stonebed.domain.missionRecord.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionRecordStatus {
    COMPLETED("COMPLETED"),
    NOT_COMPLETED("NOT_COMPLETED");

    private final String value;
}
