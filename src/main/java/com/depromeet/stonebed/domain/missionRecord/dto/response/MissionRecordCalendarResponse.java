package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.Map;

public record MissionRecordCalendarResponse(
        @Schema(description = "월별 일별 미션 기록 데이터")
                Map<String, Map<String, List<MissionRecordCalendarDto>>> data) {

    public static MissionRecordCalendarResponse from(
            Map<String, Map<String, List<MissionRecordCalendarDto>>> data) {
        return new MissionRecordCalendarResponse(data);
    }
}
