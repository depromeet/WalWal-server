package com.depromeet.stonebed.domain.missionRecord.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionStartResponse(
        @Schema(description = "미션 상태", example = "IN_PROGRESS") MissionRecordStatus status) {
    public static MissionStartResponse from(MissionRecordStatus status) {
        return new MissionStartResponse(status);
    }
}
