package com.depromeet.stonebed.global.error;

public record ErrorResponse(String message, Integer code, Object data) {
	public static ErrorResponse of(String message, Integer code, Object data) {
		return new ErrorResponse(message, code, data);
	}
}
