package com.depromeet.stonebed.domain.comment.api;

import com.depromeet.stonebed.domain.comment.application.CommentService;
import com.depromeet.stonebed.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.stonebed.domain.comment.dto.response.CommentCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "8. [댓글]", description = "댓글 관련 API입니다.")
@RequestMapping("/comments")
@RestController
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<CommentCreateResponse> commentCreate(
            @RequestBody @Valid CommentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(commentService.createComment(request));
    }

    // @Operation(summary = "댓글 조회", description = "댓글을 조회합니다.")
    // @GetMapping("/{recordId}/feed")
    // public CommentFindByRecordIdResponse commentGetList(
    // 	@PathVariable("recordId") Long recordId
    // ) {
    // 	return commentService.findCommentsByRecordId(recordId);
    // }
}
