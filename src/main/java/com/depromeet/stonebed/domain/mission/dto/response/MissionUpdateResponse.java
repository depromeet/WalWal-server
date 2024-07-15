package com.depromeet.stonebed.domain.mission.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MissionUpdateResponse(
        @Schema(description = "미션 ID", example = "1") @NotBlank Long id,
        @Schema(description = "미션 제목", example = "산책하기")
                @NotBlank(message = "Title cannot be blank")
                String title) {}
