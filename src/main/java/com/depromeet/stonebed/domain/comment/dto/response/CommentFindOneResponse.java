package com.depromeet.stonebed.domain.comment.dto.response;

import java.util.List;

public record CommentFindOneResponse(
        Long parentId,
        Long commentId,
        String content,
        Long writerId,
        String writerNickname,
        String writerProfileImageUrl,
        String createdAt,
        List<CommentFindOneResponse> children) {
    public static CommentFindOneResponse of(
            Long parentId,
            Long commentId,
            String content,
            Long writerId,
            String writerNickname,
            String writerProfileImageUrl,
            String createdAt,
            List<CommentFindOneResponse> children) {
        return new CommentFindOneResponse(
                parentId,
                commentId,
                content,
                writerId,
                writerNickname,
                writerProfileImageUrl,
                createdAt,
                children);
    }
}
