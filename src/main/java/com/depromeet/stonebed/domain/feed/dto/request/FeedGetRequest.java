package com.depromeet.stonebed.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record FeedGetRequest(
        @Schema(description = "커서 위치", example = "1") String cursor,
        @NotNull @Schema(description = "피드 당 항목 수", example = "5") int limit) {}
