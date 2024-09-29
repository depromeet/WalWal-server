package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record MissionRecordTabListResponse(
        @Schema(
                        description = "미션 탭 목록",
                        example =
                                "["
                                        + "{"
                                        + "\"recordId\": 1,"
                                        + "\"imageUrl\": \"example.jpeg\","
                                        + "\"status\": \"NOT_COMPLETED\","
                                        + "\"missionTitle\": \"산책하기\","
                                        + "\"illustrationUrl\": \"example.jpeg\","
                                        + "\"content\": \"오늘 마실다녀왔어요\","
                                        + "\"completedAt\": \"2021-10-10\""
                                        + "}"
                                        + "]")
                List<MissionTabResponse> list) {
    public static MissionRecordTabListResponse from(List<MissionTabResponse> list) {
        return new MissionRecordTabListResponse(list);
    }
}
