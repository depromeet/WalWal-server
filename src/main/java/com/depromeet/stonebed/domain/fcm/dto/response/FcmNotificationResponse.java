package com.depromeet.stonebed.domain.fcm.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FcmNotificationResponse(
        @Schema(description = "알림 리스트") List<FcmNotificationDto> list,
        @Schema(description = "다음 커서 위치", example = "2024-08-17T13:31:19") String nextCursor) {

    public static FcmNotificationResponse from(List<FcmNotificationDto> list, String nextCursor) {
        return new FcmNotificationResponse(list, nextCursor);
    }
}
