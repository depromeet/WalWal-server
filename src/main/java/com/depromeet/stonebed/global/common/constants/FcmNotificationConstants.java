package com.depromeet.stonebed.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmNotificationConstants {
    // FIRST_BOOST는 임시 -> 디자인측에서 제공시 수정예정
    FIRST_BOOST("첫 부스터 달성!", "축하합니다! 첫 번째 부스터가 추가되었어요!"),
    POPULAR("인기쟁이", "부스터를 1000개를 달성했어요!"),
    SUPER_POPULAR("최고 인기 달성", "인기폭발! 부스터를 5000개 달성했어요!"),
    MISSION_START("미션 시작!", "새로운 미션을 지금 시작해보세요!"),
    MISSION_REMINDER("미션 리마인드", "미션 종료까지 5시간 남았어요!"),
    COMMENT("댓글 알림", "님이 내 게시물에 댓글을 남겼어요!"),
    RE_COMMENT("댓글 알림", "님이 내 댓글에 대댓글을 남겼어요!"),
    RECORD_RE_COMMENT("대댓글 알림", "님이 내 게시물에 대댓글을 남겼어요!"),
    ;

    private final String title;
    private final String message;
}
