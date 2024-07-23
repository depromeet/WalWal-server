package com.depromeet.stonebed.domain.missionRecord.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordCalendarDto(
        @Schema(description = "이미지 ID") Long imageId,
        @Schema(description = "이미지 URL") String imageUrl,
        @Schema(description = "부스트 값") int boosterValue) {

    public static MissionRecordCalendarDto from(MissionRecord missionRecord, int boosterValue) {
        return new MissionRecordCalendarDto(
                missionRecord.getId(), missionRecord.getImageUrl(), boosterValue);
    }
}
