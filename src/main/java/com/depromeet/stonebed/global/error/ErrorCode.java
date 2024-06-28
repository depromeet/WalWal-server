package com.depromeet.stonebed.global.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
	SAMPLE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "샘플 에러 메시지 입니다."),
	JSON_PROCESSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 처리 중 오류가 발생했습니다.");

	private final HttpStatus httpStatus;
	private final String message;
}
