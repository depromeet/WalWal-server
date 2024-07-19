package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionRecordDayResponse(@Schema(description = "이미지 URL") String imageUrl) {

    public static MissionRecordDayResponse from(String imageUrl) {
        return new MissionRecordDayResponse(imageUrl);
    }
}
