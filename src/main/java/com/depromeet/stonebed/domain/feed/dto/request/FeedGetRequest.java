package com.depromeet.stonebed.domain.feed.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record FeedGetRequest(
        @Schema(description = "커서 위치", example = "1") String cursor,
        @Schema(description = "작성자 ID", example = "1") Long memberId,
        @Schema(description = "피드 당 항목 수", example = "5") int limit) {}
