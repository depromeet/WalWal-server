package com.depromeet.stonebed.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    SAMPLE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "샘플 에러 메시지 입니다."),
    JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 처리 중 오류가 발생했습니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다."),
    INVALID_CURSOR_DATE_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 커서 날짜 형식입니다."),
    INVALID_CURSOR_FORMAT(HttpStatus.BAD_REQUEST, "잘못된 커서 형식입니다."),

    // auth
    AUTH_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "시큐리티 인증 정보를 찾을 수 없습니다."),
    AUTHORIZATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    // apple client
    APPLE_KEY_CLIENT_FAILED(HttpStatus.BAD_REQUEST, "애플 키 생성에 실패했습니다."),
    APPLE_TOKEN_CLIENT_FAILED(HttpStatus.BAD_REQUEST, "애플 토큰 생성에 실패했습니다."),
    APPLE_PRIVATE_KEY_ENCODING_FAILED(HttpStatus.BAD_REQUEST, "애플 개인키 인코딩에 실패했습니다."),

    // kakao client
    KAKAO_TOKEN_CLIENT_FAILED(HttpStatus.BAD_REQUEST, "카카오 통신에 실패했습니다."),

    // member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    ALREADY_EXISTS_MEMBER(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),
    MEMBER_INVALID_NICKNAME(HttpStatus.BAD_REQUEST, "올바르지 않는 닉네임입니다."),
    MEMBER_ALREADY_NICKNAME(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),

    // mission
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 미션을 찾을 수 없습니다."),
    MISSION_HISTORY_NOT_FOUNT(HttpStatus.NOT_FOUND, "해당 일별 미션 정보를 찾을 수 없습니다."),
    MISSION_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 미션 기록을 찾을 수 없습니다."),
    NO_AVAILABLE_TODAY_MISSION(HttpStatus.INTERNAL_SERVER_ERROR, "할당 가능한 오늘의 미션이 없습니다."),
    DUPLICATE_MISSION_RECORD(HttpStatus.BAD_REQUEST, "오늘 완료한 미션이 존재합니다."),

    // boost
    BOOST_UNAVAILABLE_MY_FEED(HttpStatus.BAD_REQUEST, "내 피드에는 부스트를 추가할 수 없습니다."),

    // image
    IMAGE_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    IMAGE_FILE_EXTENSION_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지 파일 확장자를 찾을 수 없습니다."),
    INVALID_IMAGE_FILE_EXTENSION(HttpStatus.BAD_REQUEST, "올바른 이미지 확장자가 아닙니다."),
    MEMBER_ALREADY_DELETED(HttpStatus.CONFLICT, "이미 탈퇴한 회원입니다."),
    INVALID_IMAGE_URL(HttpStatus.BAD_REQUEST, "올바른 이미지 URL이 아닙니다."),

    // follow
    FOLLOW_SELF_NOT_ALLOWED(HttpStatus.CONFLICT, "본인은 팔로우 할 수 없습니다."),
    FOLLOW_TARGET_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "타겟 유저를 찾을 수 없습니다."),
    FOLLOW_ALREADY_EXIST(HttpStatus.BAD_REQUEST, "이미 팔로우 중인 회원입니다."),
    FOLLOW_NOT_EXIST(HttpStatus.NOT_FOUND, "팔로우 관계가 존재하지 않습니다."),

    // fcm
    INVALID_FCM_TOKEN(HttpStatus.BAD_REQUEST, "FCM 토큰값이 비어있습니다."),
    FAILED_TO_FIND_FCM_TOKEN(HttpStatus.NOT_FOUND, "해당 FCM 토큰을 찾을 수 없습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다."),

    // report
    INVALID_REPORT_REASON(HttpStatus.NOT_FOUND, "해당 신고 목록을 찾을 수 없습니다.");
    private final HttpStatus httpStatus;
    private final String message;
}
