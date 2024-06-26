package com.depromeet.stonebed.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse(String status, Object data, String message, Integer code) {

	public static ApiResponse success(Object data) {
		return new ApiResponse("success", data, null, null);
	}

	public static ApiResponse fail(Object data) {
		return new ApiResponse("fail", data, null, null);
	}

	public static ApiResponse error(String message, Integer code, Object data) {
		return new ApiResponse("error", data, message, code);
	}

	public static ApiResponse error(String message, Integer code) {
		return new ApiResponse("error", null, message, code);
	}
}
