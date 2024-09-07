package com.depromeet.stonebed.domain.fcm.dto.response;

import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

public record FcmNotificationDto(
        @Schema(description = "알림 ID", example = "1") Long notificationId,
        @Schema(description = "알림 타입", example = "MISSION") FcmNotificationType type,
        @Schema(description = "알림 제목", example = "미션 완료 알림") String title,
        @Schema(description = "알림 내용", example = "미션이 성공적으로 완료되었습니다.") String message,
        @Schema(description = "알림 이미지 URL", example = "https://example.com/image.jpg")
                String imageUrl,
        @Schema(description = "읽음 여부", example = "false") Boolean isRead,
        @Schema(description = "타겟 ID", example = "1") Long targetId,
        @Schema(description = "알림 전송 시간", example = "2024-08-17 13:31:19") LocalDateTime createdAt,
        @Schema(description = "딥링크 URL", example = "myapp://notification/1") String deepLink) {

    public static FcmNotificationDto from(
            FcmNotification notification, MissionRecord missionRecord) {
        String imageUrl = missionRecord != null ? missionRecord.getImageUrl() : null;

        return new FcmNotificationDto(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getMessage(),
                imageUrl,
                notification.getIsRead(),
                notification.getTargetId(),
                notification.getCreatedAt(),
                notification.getDeepLink());
    }
}
