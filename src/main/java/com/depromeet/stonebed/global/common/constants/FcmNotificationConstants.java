package com.depromeet.stonebed.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmNotificationConstants {
    POPULAR("인기쟁이", "게시물 부스터를 500개를 달성했어요!"),
    SUPER_POPULAR("최고 인기 달성", "인기폭발! 부스터를 5000개 달성했어요!"),
    MISSION_START("미션 시작!", "새로운 미션을 지금 시작해보세요!"),
    MISSION_REMINDER("미션 리마인드", "미션 종료까지 5시간 남았어요!");

    private final String title;
    private final String message;
}
