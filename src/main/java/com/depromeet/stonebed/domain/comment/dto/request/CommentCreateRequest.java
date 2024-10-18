package com.depromeet.stonebed.domain.comment.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record CommentCreateRequest(
        @Schema(description = "댓글 내용", example = "너무 이쁘자나~") String content,
        @Schema(description = "기록 ID", example = "1") Long recordId,
        @Schema(description = "부모 댓글 ID", example = "1") Long parentId) {}
