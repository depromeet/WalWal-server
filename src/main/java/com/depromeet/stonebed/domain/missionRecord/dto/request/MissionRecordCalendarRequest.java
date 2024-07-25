package com.depromeet.stonebed.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record MissionRecordCalendarRequest(
        @Schema(description = "커서 위치", example = "2024-01-01") String cursor,
        @NotNull @Min(1) @Schema(description = "페이지 당 항목 수", example = "30") int limit) {}
