package com.depromeet.stonebed.domain.mission.dto.response;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MissionUpdateResponse(
        @Schema(description = "미션 ID", example = "1") @NotBlank Long id,
        @Schema(description = "미션 제목", example = "산책하기")
                @NotBlank(message = "Title cannot be blank")
                String title,
        @Schema(description = "미션 완료 메시지", example = "산책 미션을 수행했어요!")
                @NotBlank(message = "CompleteMessage cannot be blank")
                String completeMessage) {
    public static MissionUpdateResponse from(Mission mission) {
        return new MissionUpdateResponse(
                mission.getId(), mission.getTitle(), mission.getCompleteMessage());
    }
}
