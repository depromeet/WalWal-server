package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordIdResponse(
        @Schema(description = "미션 기록 ID", example = "1") Long recordId) {
    public static MissionRecordIdResponse of(Long recordId) {
        return new MissionRecordIdResponse(recordId);
    }
}
