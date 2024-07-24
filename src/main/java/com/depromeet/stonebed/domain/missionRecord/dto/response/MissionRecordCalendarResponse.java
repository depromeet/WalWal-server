package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record MissionRecordCalendarResponse(
        @Schema(description = "미션 기록 데이터 리스트") Map<String, List<MissionRecordCalendarDto>> data,
        @Schema(description = "다음 데이터 커서 위치") String nextCursor) {

    public static MissionRecordCalendarResponse from(
            List<MissionRecordCalendarDto> list, String nextCursor) {
        Map<String, List<MissionRecordCalendarDto>> data = Collections.singletonMap("list", list);
        return new MissionRecordCalendarResponse(data, nextCursor);
    }
}
