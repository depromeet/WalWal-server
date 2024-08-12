package com.depromeet.stonebed.domain.fcm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FcmTokenRequest(
        @NotNull(message = "FCM 토큰은 비워둘 수 없습니다.") @Schema(description = "클라이언트의 FCM 토큰")
                String token) {}
