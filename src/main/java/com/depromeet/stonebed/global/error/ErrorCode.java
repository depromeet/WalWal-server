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

    // auth
    AUTH_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "시큐리티 인증 정보를 찾을 수 없습니다."),
    AUTHORIZATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    // apple client
    APPLE_KEY_CLIENT_FAILED(HttpStatus.BAD_REQUEST, "애플 키 생성에 실패했습니다."),
    APPLE_TOKEN_CLIENT_FAILED(HttpStatus.BAD_REQUEST, "애플 토큰 생성에 실패했습니다."),
    APPLE_PRIVATE_KEY_ENCODING_FAILED(HttpStatus.BAD_REQUEST, "애플 개인키 인코딩에 실패했습니다."),

    // member
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 회원을 찾을 수 없습니다."),
    ALREADY_EXISTS_MEMBER(HttpStatus.CONFLICT, "이미 존재하는 회원입니다."),

    // mission
    MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 미션을 찾을 수 없습니다."),
    MISSION_RECORD_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 미션 기록을 찾을 수 없습니다."),

    // image
    IMAGE_KEY_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 이미지를 찾을 수 없습니다."),
    IMAGE_FILE_EXTENSION_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지 파일 확장자를 찾을 수 없습니다."),
    ;

    private final HttpStatus httpStatus;
    private final String message;
}
