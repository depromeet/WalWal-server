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
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.common.constants.FcmNotificationConstants;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
        sendCommentNotification(missionRecord, comment, request.parentId());
        return CommentCreateResponse.of(comment.getId());
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

    private Comment createAndSaveComment(
            CommentCreateRequest request, Member member, MissionRecord missionRecord) {
        final Comment comment =
                request.parentId() != null
                        ? Comment.createComment(
                                missionRecord.getId(),
                                member,
                                request.content(),
                                findCommentById(request.parentId()))
                        : Comment.createComment(
                                missionRecord.getId(), member, request.content(), null);
        return commentRepository.save(comment);
    }

    private void sendCommentNotification(
            MissionRecord missionRecord, Comment comment, Long parentId) {
        Member missionRecordOwner = missionRecord.getMember();
        Member commentWriter = comment.getWriter();

        if (isRootComment(parentId, missionRecordOwner, commentWriter)) {
            sendCommentNotification(
                    missionRecordOwner,
                    FcmNotificationConstants.COMMENT,
                    missionRecord,
                    comment,
                    commentWriter);
        } else if (isReplyComment(comment)) {
            sendReplyNotifications(missionRecord, comment, commentWriter);
        }
    }

    private boolean isRootComment(Long parentId, Member missionRecordOwner, Member commentWriter) {
        return !missionRecordOwner.equals(commentWriter) && parentId == null;
    }

    private boolean isReplyComment(Comment comment) {
        return comment.getParent() != null;
    }

    private void sendReplyNotifications(
            MissionRecord missionRecord, Comment comment, Member commentWriter) {
        Member parentCommentWriter = comment.getParent().getWriter();
        Member missionRecordOwner = missionRecord.getMember();

        if (!missionRecordOwner.equals(commentWriter)) {
            sendCommentNotification(
                    missionRecordOwner,
                    FcmNotificationConstants.RECORD_RE_COMMENT,
                    missionRecord,
                    comment,
                    commentWriter);
        } else if (!parentCommentWriter.equals(commentWriter)) {
            sendCommentNotification(
                    parentCommentWriter,
                    FcmNotificationConstants.RE_COMMENT,
                    missionRecord,
                    comment,
                    commentWriter);
        }

        Set<Member> commentRecipients = collectNotificationRecipients(comment);
        commentRecipients.remove(parentCommentWriter);

        for (Member recipient : commentRecipients) {
            sendCommentNotification(
                    recipient,
                    FcmNotificationConstants.RE_COMMENT,
                    missionRecord,
                    comment,
                    commentWriter);
        }
    }

    private void sendCommentNotification(
            Member recipient,
            FcmNotificationConstants notificationType,
            MissionRecord missionRecord,
            Comment comment,
            Member commentWriter) {
        String title = notificationType.getTitle();
        String message = commentWriter.getProfile().getNickname() + notificationType.getMessage();
        List<String> tokens = retrieveFcmTokens(Set.of(recipient));
        String notificationTypeName =
                notificationType.name().equals(FcmNotificationConstants.RECORD_RE_COMMENT.name())
                        ? FcmNotificationConstants.RE_COMMENT.name()
                        : notificationType.name();
        fcmNotificationService.sendAndNotifications(
                title,
                message,
                tokens,
                comment.getId(),
                missionRecord.getId(),
                FcmNotificationType.valueOf(notificationTypeName));
    }

    private Set<Member> collectNotificationRecipients(Comment comment) {
        Map<Long, Member> notificationRecipientsMap = new HashMap<>();
        Comment currentComment = comment;

        while (currentComment.getParent() != null) {
            currentComment = currentComment.getParent();
            currentComment.getReplyComments().stream()
                    .map(Comment::getWriter)
                    .forEach(writer -> notificationRecipientsMap.put(writer.getId(), writer));
        }

        notificationRecipientsMap.remove(comment.getWriter().getId());
        return new HashSet<>(notificationRecipientsMap.values());
    }

    private List<String> retrieveFcmTokens(Set<Member> notificationRecipients) {
        return notificationRecipients.stream()
                .flatMap(member -> fcmTokenRepository.findByMember(member).stream())
                .map(FcmToken::getToken)
                .filter(Objects::nonNull)
                .filter(token -> !token.isEmpty() && !token.isBlank())
                .collect(Collectors.toList());
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
                                }));
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

        // 작성자가 null인지 확인
        Long writerId =
                comment.getWriter().getStatus() != MemberStatus.DELETED
                        ? comment.getWriter().getId()
                        : null;
        String writerNickname =
                comment.getWriter().getStatus() != MemberStatus.DELETED
                        ? comment.getWriter().getProfile().getNickname()
                        : "탈퇴한 회원";
        String writerProfileImageUrl =
                comment.getWriter().getStatus() != MemberStatus.DELETED
                        ? comment.getWriter().getProfile().getProfileImageUrl()
                        : "INACTIVE_" + comment.getWriter().getRaisePet().getValue();

        return CommentFindOneResponse.of(
                comment.getParent() != null ? comment.getParent().getId() : null,
                comment.getId(),
                comment.getContent(),
                writerId,
                writerNickname,
                writerProfileImageUrl,
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
