package com.depromeet.stonebed.domain.missionRecord.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MissionCompleteResponse(@Schema(description = "미션 이미지") String missionImageUrl) {

    public static MissionCompleteResponse from(String missionImageUrl) {
        return new MissionCompleteResponse(missionImageUrl);
    }
}
