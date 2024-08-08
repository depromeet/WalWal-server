package com.depromeet.stonebed.domain.feed.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FeedGetResponse(
        List<FeedContentGetResponse> list,
        @Schema(description = "커서 위치", example = "1") String nextCursor) {
    public static FeedGetResponse from(List<FeedContentGetResponse> list, String nextCursor) {
        return new FeedGetResponse(list, nextCursor);
    }
}
