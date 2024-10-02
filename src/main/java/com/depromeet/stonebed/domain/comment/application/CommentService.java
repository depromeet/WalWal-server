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
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.common.constants.FcmNotificationConstants;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
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

    public CommentCreateResponse createComment(CommentCreateRequest request) {
        final Member member = memberUtil.getCurrentMember();
        final MissionRecord missionRecord = findMissionRecordById(request.recordId());

        final Comment comment = createAndSaveComment(request, member, missionRecord);
        final FcmNotificationConstants notificationType = getNotificationType(request);

        sendCommentNotification(missionRecord, comment, notificationType);
        return CommentCreateResponse.of(comment.getId());
    }

    private Comment createAndSaveComment(
            CommentCreateRequest request, Member member, MissionRecord missionRecord) {
        final Comment comment =
                request.parentId() != null
                        ? Comment.createComment(
                                missionRecord,
                                member,
                                request.content(),
                                findCommentById(request.parentId()))
                        : Comment.createComment(missionRecord, member, request.content(), null);

        return commentRepository.save(comment);
    }

    private FcmNotificationConstants getNotificationType(CommentCreateRequest request) {
        return request.parentId() == null
                ? FcmNotificationConstants.COMMENT
                : FcmNotificationConstants.RE_COMMENT;
    }

    private void sendCommentNotification(
            MissionRecord missionRecord,
            Comment comment,
            FcmNotificationConstants commentNotification) {
        Set<Member> notificationRecipients = collectNotificationRecipients(missionRecord, comment);
        List<String> tokens = retrieveFcmTokens(notificationRecipients);

        FcmNotificationType fcmNotificationType = getFcmNotificationType(commentNotification);
        fcmNotificationService.sendAndNotifications(
                commentNotification.getTitle(),
                commentNotification.getMessage(),
                tokens,
                fcmNotificationType);
    }

    private Set<Member> collectNotificationRecipients(
            MissionRecord missionRecord, Comment comment) {
        Set<Member> notificationRecipients = new HashSet<>();
        notificationRecipients.add(missionRecord.getMember());

        Comment currentComment = comment;
        while (currentComment.getParent() != null) {
            currentComment = currentComment.getParent();
            currentComment.getReplyComments().stream()
                    .map(Comment::getWriter)
                    .forEach(notificationRecipients::add);
        }
        notificationRecipients.remove(comment.getWriter());
        return notificationRecipients;
    }

    private List<String> retrieveFcmTokens(Set<Member> notificationRecipients) {
        return notificationRecipients.stream()
                .map(fcmTokenRepository::findByMember)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(FcmToken::getToken)
                .filter(Objects::nonNull)
                .filter(token -> !token.isEmpty() && !token.isBlank())
                .collect(Collectors.toList());
    }

    private FcmNotificationType getFcmNotificationType(
            FcmNotificationConstants commentNotification) {
        return FcmNotificationConstants.RE_COMMENT.equals(commentNotification)
                ? FcmNotificationType.RE_COMMENT
                : FcmNotificationType.COMMENT;
    }

    @Transactional(readOnly = true)
    public CommentFindResponse findCommentsByRecordId(Long recordId) {
        final MissionRecord missionRecord = findMissionRecordById(recordId);
        final List<Comment> allComments =
                commentRepository.findAllCommentsByMissionRecord(missionRecord);

        Map<Long, List<Comment>> commentsByParentId = groupCommentsByParentId(allComments);
        List<CommentFindOneResponse> rootResponses =
                convertToCommentFindOneResponses(commentsByParentId);

        return CommentFindResponse.of(rootResponses);
    }

    private Map<Long, List<Comment>> groupCommentsByParentId(List<Comment> allComments) {
        return allComments.stream()
                .collect(
                        Collectors.groupingBy(
                                comment -> {
                                    Comment parent = comment.getParent();
                                    return (parent != null)
                                            ? parent.getId()
                                            : ROOT_COMMENT_PARENT_ID;
                                },
                                Collectors.toList()));
    }

    private List<CommentFindOneResponse> convertToCommentFindOneResponses(
            Map<Long, List<Comment>> commentsByParentId) {
        List<Comment> rootComments =
                commentsByParentId.getOrDefault(ROOT_COMMENT_PARENT_ID, List.of());
        return rootComments.stream()
                .map(comment -> convertToCommentFindOneResponse(comment, commentsByParentId))
                .collect(Collectors.toList());
    }

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
