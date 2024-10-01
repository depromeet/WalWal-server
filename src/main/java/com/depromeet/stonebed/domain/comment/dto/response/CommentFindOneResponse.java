package com.depromeet.stonebed.domain.comment.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

public record CommentFindOneResponse(
        @Schema(description = "부모 댓글 ID", example = "1") Long parentId,
        @Schema(description = "댓글 ID", example = "1") Long commentId,
        @Schema(description = "댓글 내용", example = "너무 이쁘자나~") String content,
        @Schema(description = "작성자 ID", example = "1") Long writerId,
        @Schema(description = "작성자 닉네임", example = "왈왈대장") String writerNickname,
        @Schema(description = "작성자 프로필 이미지 URL", example = "https://default.walwal/profile.jpg")
                String writerProfileImageUrl,
        @Schema(description = "작성일", example = "2021-10-01T00:00:00") String createdAt,
        @Schema(description = "자식 댓글 목록") List<CommentFindOneResponse> children) {
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
