package com.depromeet.stonebed.domain.mission.dto;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import io.swagger.v3.oas.annotations.media.Schema;

public record MissionDTO(
        @Schema(description = "미션 ID", example = "1") Long id,
        @Schema(description = "미션 제목", example = "산책하기") String title) {
    public static MissionDTO from(Mission mission) {
        return new MissionDTO(mission.getId(), mission.getTitle());
    }
}
