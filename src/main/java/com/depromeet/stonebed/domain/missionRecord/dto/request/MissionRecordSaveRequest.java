package com.depromeet.stonebed.domain.missionRecord.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordSaveRequest(
        @Schema(description = "미션 소감", example = "너무 귀엽다...") String text) {}
