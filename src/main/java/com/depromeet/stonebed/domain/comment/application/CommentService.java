package com.depromeet.stonebed.domain.comment.application;

import com.depromeet.stonebed.domain.comment.dao.CommentRepository;
import com.depromeet.stonebed.domain.comment.domain.Comment;
import com.depromeet.stonebed.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.stonebed.domain.comment.dto.response.CommentCreateResponse;
import com.depromeet.stonebed.domain.comment.dto.response.CommentFindOneResponse;
import com.depromeet.stonebed.domain.comment.dto.response.CommentFindResponse;
import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final MemberUtil memberUtil;
    private final CommentRepository commentRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final FcmNotificationService fcmNotificationService;

    /**
     * CommentCreateRequest 댓글을 생성합니다.
     *
     * @param request content recordId parentId
     * @return
     */
    public CommentCreateResponse createComment(CommentCreateRequest request) {
        final Member member = memberUtil.getCurrentMember();
        final MissionRecord missionRecord = findMissionRecordById(request.recordId());
        if (request.parentId() != null) {
            // 부모 댓글이 존재하는 경우
            final Comment parentComment = findCommentById(request.parentId());
            final Comment comment =
                    Comment.createCommentChild(
                            missionRecord, member, request.content(), parentComment);
            Comment saveComment = commentRepository.save(comment);
            return CommentCreateResponse.of(saveComment.getId());
        } else {
            // 부모 댓글이 존재하지 않는 경우, 부모 댓글 생성
            final Comment comment =
                    Comment.createCommentParent(missionRecord, member, request.content());
            Comment saveComment = commentRepository.save(comment);
            return CommentCreateResponse.of(saveComment.getId());
        }
    }

    public CommentFindResponse findCommentsByRecordId(Long recordId) {
        final MissionRecord missionRecord = findMissionRecordById(recordId);
        final List<Comment> allComments =
                commentRepository.findAllCommentsByMissionRecord(missionRecord);

        Map<Long, List<Comment>> commentsByParentId =
                allComments.stream()
                        .collect(
                                Collectors.groupingBy(
                                        comment -> {
                                            Comment parent = comment.getParent();
                                            // null 처리 대신 -1로 처리하여 root 댓글 구분
                                            return (parent != null)
                                                    ? parent.getId()
                                                    : -1L; // Use -1L for null parent IDs
                                        },
                                        Collectors.toList()));

        List<Comment> rootComments = commentsByParentId.get(-1L);
        if (rootComments == null) {
            rootComments = List.of();
        }

        // Convert root comments to CommentFindOneResponse
        List<CommentFindOneResponse> rootResponses =
                rootComments.stream()
                        .map(
                                comment ->
                                        convertToCommentFindOneResponse(
                                                comment, commentsByParentId))
                        .collect(Collectors.toList());

        return CommentFindResponse.of(rootResponses);
    }

    private CommentFindOneResponse convertToCommentFindOneResponse(
            Comment comment, Map<Long, List<Comment>> commentsByParentId) {
        List<CommentFindOneResponse> childrenResponses =
                commentsByParentId.getOrDefault(comment.getId(), List.of()).stream()
                        .map(
                                childComment ->
                                        convertToCommentFindOneResponse(
                                                childComment, commentsByParentId))
                        .collect(Collectors.toList());

        return CommentFindOneResponse.of(
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getId(),
                comment.getContent(),
                comment.getWriter().getId(),
                comment.getWriter().getProfile().getNickname(),
                comment.getWriter().getProfile().getProfileImageUrl(),
                comment.getCreatedAt().toString(),
                childrenResponses);
    }

    private MissionRecord findMissionRecordById(Long recordId) {
        return missionRecordRepository
                .findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));
    }

    private Comment findCommentById(Long commentId) {
        return commentRepository
                .findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
