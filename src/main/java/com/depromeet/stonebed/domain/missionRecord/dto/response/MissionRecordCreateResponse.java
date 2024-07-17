package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordCreateResponse(
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long recordId,
        @Schema(description = "미션 제목") String missionTitle) {

    public static MissionRecordCreateResponse from(Long recordId, String missionTitle) {
        return new MissionRecordCreateResponse(recordId, missionTitle);
    }
}
