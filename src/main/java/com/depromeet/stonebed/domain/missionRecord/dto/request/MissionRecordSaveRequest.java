package com.depromeet.stonebed.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MissionRecordSaveRequest(
        @NotNull @Schema(description = "미션 ID", example = "1") Long missionId,
        @Schema(description = "미션 소감", example = "너무 귀엽다...") String content) {}
