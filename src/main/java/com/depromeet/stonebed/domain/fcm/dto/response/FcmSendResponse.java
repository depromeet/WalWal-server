package com.depromeet.stonebed.domain.fcm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FcmSendResponse(
        @Schema(description = "인증 토큰") String token,
        @Schema(description = "푸시 알림의 제목") String title,
        @Schema(description = "푸시 알림의 내용") String body) {

    public static FcmSendResponse of(String token, String title, String body) {
        return new FcmSendResponse(token, title, body);
    }
}
