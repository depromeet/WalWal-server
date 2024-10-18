package com.depromeet.stonebed.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentCreateResponse(@Schema(description = "댓글 ID", example = "1") Long commentId) {
    public static CommentCreateResponse of(Long commentId) {
        return new CommentCreateResponse(commentId);
    }
}
