package com.depromeet.stonebed.domain.feed.dto.response.v2;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record FeedGetResponseV2(
        List<FeedContentGetResponseV2> list,
        @Schema(description = "커서 위치", example = "1") String nextCursor) {
    public static FeedGetResponseV2 from(List<FeedContentGetResponseV2> list, String nextCursor) {
        return new FeedGetResponseV2(list, nextCursor);
    }
}
