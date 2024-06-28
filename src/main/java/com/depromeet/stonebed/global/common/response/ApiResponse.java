package com.depromeet.stonebed.global.common.response;

import com.depromeet.stonebed.global.error.ErrorResponse;

import lombok.Getter;

@Getter
public record ApiResponse(boolean success, int status, Object data) {

	public static ApiResponse success(int status, Object data) {
		return new ApiResponse(true, status, data);
	}

	public static ApiResponse fail(int status, ErrorResponse errorResponse) {
		return new ApiResponse(false, status, errorResponse);
	}
}
