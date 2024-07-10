package com.depromeet.stonebed.domain.mission.api;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MissionUpdateRequest {
    @NotBlank(message = "Title cannot be blank")
    private String title;
}
