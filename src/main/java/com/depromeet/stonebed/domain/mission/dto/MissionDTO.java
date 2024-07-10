package com.depromeet.stonebed.domain.mission.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class MissionDTO {
    private Long id;
    private String title;
}
