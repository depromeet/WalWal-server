package com.depromeet.stonebed.domain.mission.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

public record MissionUpdateRequest(
        @Schema(description = "미션 제목", example = "산책하기")
                @NotBlank(message = "Title cannot be blank")
                String title) {}
