package com.depromeet.stonebed.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MissionRecordBoostRequest(
        @NotNull @Min(value = 1) @Max(value = 500) @Schema(description = "부스트 카운트", example = "1")
                Long count) {}
