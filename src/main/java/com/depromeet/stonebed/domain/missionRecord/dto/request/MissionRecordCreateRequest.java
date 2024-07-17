package com.depromeet.stonebed.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MissionRecordCreateRequest(
        @NotNull @Schema(description = "미션 아이디", defaultValue = "1") Long missionId) {}
