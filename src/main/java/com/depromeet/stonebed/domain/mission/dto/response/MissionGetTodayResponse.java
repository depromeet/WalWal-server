package com.depromeet.stonebed.domain.mission.dto.response;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MissionGetTodayResponse(
        @Schema(description = "미션 ID", example = "1") @NotBlank Long id,
        @Schema(description = "미션 제목", example = "산책하기")
                @NotBlank(message = "Title cannot be blank")
                String title,
        @Schema(description = "일러스트 이미지 URL", example = "https://image.walwal.life/image.jpeg")
                String illustrationUrl) {

    public static MissionGetTodayResponse from(Mission mission) {
        return new MissionGetTodayResponse(
                mission.getId(), mission.getTitle(), mission.getIllustrationUrl());
    }
}
