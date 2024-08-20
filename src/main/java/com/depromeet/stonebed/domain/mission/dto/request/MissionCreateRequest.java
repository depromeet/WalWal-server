package com.depromeet.stonebed.domain.mission.dto.request;

import com.depromeet.stonebed.domain.member.domain.RaisePet;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record MissionCreateRequest(
        @Schema(description = "미션 제목", example = "산책하기")
                @NotBlank(message = "Title cannot be blank")
                String title,
        @Schema(description = "반려동물 유형", example = "DOG")
                @NotNull(message = "RaisePet cannot be null")
                RaisePet raisePet) {}
