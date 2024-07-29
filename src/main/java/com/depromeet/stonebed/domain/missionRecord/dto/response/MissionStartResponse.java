package com.depromeet.stonebed.domain.missionRecord.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionStartResponse(
        @Schema(description = "미션 상태", example = "IN_PROGRESS") MissionStatus status) {
    public static MissionStartResponse from(MissionStatus status) {
        return new MissionStartResponse(status);
    }
}
