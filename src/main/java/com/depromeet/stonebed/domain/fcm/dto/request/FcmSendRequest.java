package com.depromeet.stonebed.domain.fcm.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FcmSendRequest(
        @NotNull(message = "제목은 비워둘 수 없습니다.")
                @Schema(description = "푸시 알림의 제목", example = "새로운 알림이 도착했습니다")
                String title,
        @NotNull(message = "내용은 비워둘 수 없습니다.")
                @Schema(description = "푸시 알림의 내용", example = "안녕하세요, 새로운 알림이 있습니다!")
                String body) {}
