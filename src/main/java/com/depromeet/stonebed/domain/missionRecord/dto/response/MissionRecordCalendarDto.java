package com.depromeet.stonebed.domain.missionRecord.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.format.DateTimeFormatter;

public record MissionRecordCalendarDto(
        @Schema(description = "미션 기록 ID") Long recordId,
        @Schema(description = "이미지 URL") String imageUrl,
        @Schema(description = "미션 수행 일자") String missionDate) {

    public static MissionRecordCalendarDto from(
            MissionRecord missionRecord, DateTimeFormatter formatter) {
        String formattedDate = missionRecord.getCreatedAt().format(formatter);
        return new MissionRecordCalendarDto(
                missionRecord.getId(), missionRecord.getImageUrl(), formattedDate);
    }
}
