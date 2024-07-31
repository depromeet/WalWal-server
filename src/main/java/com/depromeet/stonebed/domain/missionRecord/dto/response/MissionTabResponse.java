package com.depromeet.stonebed.domain.missionRecord.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionTabResponse(
        @Schema(description = "미션 상태", example = "NOT_COMPLETED") MissionRecordStatus status) {
    public static MissionTabResponse from(MissionRecordStatus status) {
        return new MissionTabResponse(status);
    }
}
