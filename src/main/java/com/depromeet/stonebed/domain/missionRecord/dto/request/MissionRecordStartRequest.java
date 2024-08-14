package com.depromeet.stonebed.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MissionRecordStartRequest(
        @NotNull @Schema(description = "미션 ID", example = "1") Long missionId) {}
