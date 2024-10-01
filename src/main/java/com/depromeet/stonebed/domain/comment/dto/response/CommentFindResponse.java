package com.depromeet.stonebed.domain.comment.dto.response;

import java.util.List;

public record CommentFindResponse(List<CommentFindOneResponse> comments) {
    public static CommentFindResponse of(List<CommentFindOneResponse> comments) {
        return new CommentFindResponse(comments);
    }
}
