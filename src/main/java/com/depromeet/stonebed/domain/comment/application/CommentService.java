package com.depromeet.stonebed.domain.comment.application;

import com.depromeet.stonebed.domain.comment.dao.CommentRepository;
import com.depromeet.stonebed.domain.comment.domain.Comment;
import com.depromeet.stonebed.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.stonebed.domain.comment.dto.response.CommentCreateResponse;
import com.depromeet.stonebed.domain.comment.dto.response.CommentFindOneResponse;
import com.depromeet.stonebed.domain.comment.dto.response.CommentFindResponse;
import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.fcm.dao.FcmTokenRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.common.constants.FcmNotificationConstants;
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
    private final FcmTokenRepository fcmTokenRepository;
    private static final Long ROOT_COMMENT_PARENT_ID = -1L;

    /**
     * 댓글을 생성합니다.
     *
     * @param request 댓글 생성 요청 객체 (content, recordId, parentId 포함)
     * @return 생성된 댓글의 ID를 포함한 응답 객체
     */
    public CommentCreateResponse createComment(CommentCreateRequest request) {
        final Member member = memberUtil.getCurrentMember();
        final MissionRecord missionRecord = findMissionRecordById(request.recordId());

        // 부모 댓글이 존재하는 경우

        final Comment comment =
                request.parentId() != null
                        ? Comment.createComment(
                                missionRecord,
                                member,
                                request.content(),
                                findCommentById(request.parentId()))
                        : Comment.createComment(missionRecord, member, request.content(), null);

        Comment savedComment = commentRepository.save(comment);
        FcmNotificationConstants notificationConstants = FcmNotificationConstants.COMMENT;

        fcmTokenRepository
                .findByMember(member)
                .ifPresent(
                        fcmToken -> {
                            fcmNotificationService.sendAndNotifications(
                                    notificationConstants.getTitle(),
                                    notificationConstants.getMessage(),
                                    List.of(fcmToken.getToken()),
                                    FcmNotificationType.COMMENT);
                        });

        return CommentCreateResponse.of(savedComment.getId());
    }

    /**
     * 특정 기록 ID에 대한 모든 댓글을 조회합니다.
     *
     * @param recordId 조회할 기록의 ID
     * @return 조회된 댓글 목록을 포함한 응답 객체
     */
    @Transactional(readOnly = true)
    public CommentFindResponse findCommentsByRecordId(Long recordId) {
        final MissionRecord missionRecord = findMissionRecordById(recordId);
        final List<Comment> allComments =
                commentRepository.findAllCommentsByMissionRecord(missionRecord);

        // 댓글을 부모 ID로 그룹화, 부모 ID가 null인 경우 -1L로 처리
        Map<Long, List<Comment>> commentsByParentId =
                allComments.stream()
                        .collect(
                                Collectors.groupingBy(
                                        comment -> {
                                            Comment parent = comment.getParent();
                                            return (parent != null)
                                                    ? parent.getId()
                                                    : ROOT_COMMENT_PARENT_ID;
                                        },
                                        Collectors.toList()));

        // 부모 댓글 (부모 댓글이 없는 댓글) 조회
        List<Comment> rootComments =
                commentsByParentId.getOrDefault(ROOT_COMMENT_PARENT_ID, List.of());

        // 부모 댓글을 CommentFindOneResponse로 변환
        List<CommentFindOneResponse> rootResponses =
                rootComments.stream()
                        .map(
                                comment ->
                                        convertToCommentFindOneResponse(
                                                comment, commentsByParentId))
                        .collect(Collectors.toList());

        return CommentFindResponse.of(rootResponses);
    }

    /**
     * Comment 객체를 CommentFindOneResponse 객체로 변환합니다.
     *
     * @param comment 변환할 댓글 객체
     * @param commentsByParentId 부모 ID로 그룹화된 댓글 key-value 형태의 Map
     * @return 변환된 댓글 응답 객체
     */
    private CommentFindOneResponse convertToCommentFindOneResponse(
            Comment comment, Map<Long, List<Comment>> commentsByParentId) {
        List<CommentFindOneResponse> replyCommentsResponses =
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
                replyCommentsResponses);
    }

    /**
     * MissionRecord를 조회
     *
     * @param recordId 조회할 기록의 ID
     * @return 조회된 MissionRecord 객체
     * @throws CustomException 기록을 찾을 수 없는 경우 예외 발생
     */
    private MissionRecord findMissionRecordById(Long recordId) {
        return missionRecordRepository
                .findById(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));
    }

    /**
     * Comment를 조회
     *
     * @param commentId 조회할 댓글의 ID
     * @return 조회된 Comment 객체
     * @throws CustomException 댓글을 찾을 수 없는 경우 예외 발생
     */
    private Comment findCommentById(Long commentId) {
        return commentRepository
                .findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }
}
