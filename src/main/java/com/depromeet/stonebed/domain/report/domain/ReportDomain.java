package com.depromeet.stonebed.domain.report.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportDomain {
    MISSION_RECORD("미션 기록"),
    ;
    private final String value;
}
