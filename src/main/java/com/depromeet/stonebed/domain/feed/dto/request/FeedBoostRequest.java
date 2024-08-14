package com.depromeet.stonebed.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FeedBoostRequest(
        @NotNull @Max(value = 500) @Schema(description = "부스트 카운트", example = "1") Long count,
        @NotNull @Min(value = 1) @Schema(description = "미션 기록 ID", example = "1")
                Long missionRecordId) {}
