package com.depromeet.stonebed.domain.record.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MissionStatus {
    COMPLETED("COMPLETED"),
    NOT_COMPLETED("NOT_COMPLETED");

    private final String value;
}
