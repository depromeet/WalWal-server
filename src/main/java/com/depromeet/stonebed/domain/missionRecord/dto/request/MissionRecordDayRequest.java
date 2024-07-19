package com.depromeet.stonebed.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record MissionRecordDayRequest(
        @Schema(description = "조회할 날짜", example = "2024-07-19") LocalDate date) {}
