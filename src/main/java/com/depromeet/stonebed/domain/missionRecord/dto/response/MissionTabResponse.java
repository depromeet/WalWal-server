package com.depromeet.stonebed.domain.missionRecord.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionTabResponse(
        @Schema(description = "미션기록 ID", example = "1") Long recordId,
        @Schema(description = "이미지 URL", example = "example.jpeg") String imageUrl,
        @Schema(description = "미션 상태", example = "NOT_COMPLETED") MissionRecordStatus status) {

    public static MissionTabResponse from(
            Long recordId, String imageUrl, MissionRecordStatus status) {
        return new MissionTabResponse(recordId, imageUrl, status);
    }
}
