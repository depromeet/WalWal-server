package com.depromeet.stonebed.global.error.exception;

import com.depromeet.stonebed.global.common.response.ApiResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse> handleCustomException(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();
        log.error("CustomException: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse =
                ErrorResponse.of(errorCode.name(), errorCode.getHttpStatus().value(), null);
        ApiResponse apiResponse =
                ApiResponse.fail(errorCode.getHttpStatus().value(), errorResponse);
        return ResponseEntity.status(errorCode.getHttpStatus()).body(apiResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleAllExceptions(Exception ex, WebRequest request) {
        log.error("Exception: {}", ex.getMessage(), ex);
        ErrorResponse errorResponse =
                ErrorResponse.of(
                        "서버에서 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value(), null);
        ApiResponse response =
                ApiResponse.fail(HttpStatus.INTERNAL_SERVER_ERROR.value(), errorResponse);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
