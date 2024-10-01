package com.depromeet.stonebed.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CommentFindResponse(
        @Schema(description = "댓글 목록") List<CommentFindOneResponse> comments) {
    public static CommentFindResponse of(List<CommentFindOneResponse> comments) {
        return new CommentFindResponse(comments);
    }
}
