package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public record MissionRecordCalendarResponse(
        @Schema(
                        description = "미션 기록 데이터 리스트",
                        example =
                                "{ 'list': [{ 'imageId': 1, 'imageUrl': 'http://example.com/image1.jpg', 'missionDate': '2024-01-01' }]}")
                Map<String, List<MissionRecordCalendarDto>> data,
        @Schema(description = "커서 위치", example = "2024-01-03") String nextCursor) {

    public static MissionRecordCalendarResponse from(
            List<MissionRecordCalendarDto> list, String nextCursor) {
        Map<String, List<MissionRecordCalendarDto>> data = Collections.singletonMap("list", list);
        return new MissionRecordCalendarResponse(data, nextCursor);
    }
}
