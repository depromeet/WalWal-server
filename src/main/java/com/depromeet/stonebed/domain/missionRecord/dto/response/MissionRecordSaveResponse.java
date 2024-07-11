package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordSaveResponse(
        @Schema(description = "미션 기록 ID", defaultValue = "1") Long missionId) {

    public static MissionRecordSaveResponse from(Long missionId) {
        return new MissionRecordSaveResponse(missionId);
    }
}
