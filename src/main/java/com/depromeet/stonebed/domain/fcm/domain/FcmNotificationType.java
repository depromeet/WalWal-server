package com.depromeet.stonebed.domain.fcm.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmNotificationType {
    MISSION("미션 알림"),
    BOOSTER("부스터 알림");

    private final String value;

    public String getValue() {
        return value;
    }
}
