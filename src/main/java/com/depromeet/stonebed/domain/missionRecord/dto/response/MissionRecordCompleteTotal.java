package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordCompleteTotal(
        @Schema(description = "수행한 총 미션 기록 수", example = "123") Long totalCount) {

    public static MissionRecordCompleteTotal of(Long totalCount) {
        return new MissionRecordCompleteTotal(totalCount);
    }
}
