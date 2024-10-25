package com.depromeet.stonebed.domain.comment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.comment.dao.CommentRepository;
import com.depromeet.stonebed.domain.comment.domain.Comment;
import com.depromeet.stonebed.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.stonebed.domain.comment.dto.response.CommentCreateResponse;
import com.depromeet.stonebed.domain.comment.dto.response.CommentFindOneResponse;
import com.depromeet.stonebed.domain.comment.dto.response.CommentFindResponse;
import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.fcm.dao.FcmTokenRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class CommentServiceTest extends FixtureMonkeySetUp {

    @Mock private MemberUtil memberUtil;

    @Mock private CommentRepository commentRepository;

    @Mock private MissionRecordRepository missionRecordRepository;

    @Mock private FcmNotificationService fcmNotificationService;

    @Mock private FcmTokenRepository fcmTokenRepository;

    @InjectMocks private CommentService commentService;

    private static final int CHILD_COMMENT_COUNT = 5; // 자식 댓글 생성 횟수

    // 생성
    @Test
    void 부모_댓글_생성합니다() {
        // given
        Long recordId = 1L;
        String content = "너무 이쁘자나~";

        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);
        CommentCreateRequest request = CommentCreateRequest.of(content, recordId, null);
        Comment comment = createMockParentComment(member, missionRecord, content);

        mockCommonDependencies(member, missionRecord, recordId, comment);

        // when
        CommentCreateResponse response = commentService.createComment(request);

        // then
        assertNotNull(response);
        assertEquals(comment.getId(), response.commentId());

        verifyCommonInvocations(recordId);
    }

    @Test
    void 자식_댓글_생성합니다() {
        // given
        String content = "너무 이쁘자나~";
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);
        Long recordId = missionRecord.getId();
        Comment parentComment = createMockParentComment(member, missionRecord, content);

        mockCommonDependencies(member, missionRecord, recordId, parentComment);

        // when: 부모 댓글 생성
        CommentCreateResponse parentResponse =
                commentService.createComment(CommentCreateRequest.of(content, recordId, null));

        // 부모 댓글 조회
        when(commentRepository.findById(parentResponse.commentId()))
                .thenReturn(Optional.of(parentComment));

        // 자식 댓글 생성 및 검증
        Comment childComment = createMockComment(member, missionRecord, "자식 댓글 내용", parentComment);
        when(commentRepository.save(any(Comment.class))).thenReturn(childComment);

        CommentCreateResponse childResponse =
                commentService.createComment(
                        CommentCreateRequest.of(
                                "자식 댓글 내용", missionRecord.getId(), parentResponse.commentId()));

        // then: 검증
        verifyCommonInvocations(recordId, 2); // 부모 + 자식 댓글 생성
        verify(commentRepository, times(2)).save(any(Comment.class));

        assertChildComment(childResponse, parentResponse, childComment, parentComment);
    }

    @Test
    void 부모_댓글에_여러_자식_댓글을_생성합니다() {
        // given
        String content = "부모 댓글 내용";
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);
        Long recordId = missionRecord.getId();
        Comment parentComment = createMockParentComment(member, missionRecord, content);

        mockCommonDependencies(member, missionRecord, recordId, parentComment);

        CommentCreateResponse parentResponse =
                commentService.createComment(CommentCreateRequest.of(content, recordId, null));

        // 부모 댓글 조회
        when(commentRepository.findById(parentResponse.commentId()))
                .thenReturn(Optional.of(parentComment));

        // 자식 댓글 생성 및 검증
        List<CommentCreateResponse> childResponses = new ArrayList<>();
        for (int i = 1; i <= CHILD_COMMENT_COUNT; i++) {
            Comment childComment =
                    createMockComment(member, missionRecord, "자식 댓글 내용 " + i, parentComment);
            when(commentRepository.save(any(Comment.class))).thenReturn(childComment);

            CommentCreateResponse childResponse =
                    commentService.createComment(
                            CommentCreateRequest.of(
                                    "자식 댓글 내용 " + i,
                                    missionRecord.getId(),
                                    parentResponse.commentId()));
            childResponses.add(childResponse);

            assertChildComment(childResponse, parentResponse, childComment, parentComment);
        }

        // then: 모든 자식 댓글의 ID가 유일한지 확인
        verifyCommonInvocations(recordId, CHILD_COMMENT_COUNT + 1); // 부모 + 자식 댓글 생성
        verify(commentRepository, times(CHILD_COMMENT_COUNT + 1)).save(any(Comment.class));

        Set<Long> uniqueChildCommentIds =
                childResponses.stream()
                        .map(CommentCreateResponse::commentId)
                        .collect(Collectors.toSet());
        assertEquals(
                CHILD_COMMENT_COUNT, uniqueChildCommentIds.size()); // 자식 댓글 수와 고유한 ID 수가 동일해야 함
    }

    // 중복된 모의 객체 설정을 처리하는 메서드
    private void mockCommonDependencies(
            Member member, MissionRecord missionRecord, Long recordId, Comment comment) {
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.of(missionRecord));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
    }

    // 부모/자식 댓글 공통 검증 메서드
    private void assertChildComment(
            CommentCreateResponse childResponse,
            CommentCreateResponse parentResponse,
            Comment childComment,
            Comment parentComment) {
        assertNotNull(childResponse);
        assertNotNull(childResponse.commentId());
        assertNotEquals(
                parentResponse.commentId(), childResponse.commentId()); // 부모와 자식 댓글 ID는 달라야 함
        assertEquals(childComment.getId(), childResponse.commentId()); // 자식 댓글 ID 확인
        assertEquals(parentComment.getId(), childComment.getParent().getId()); // 자식 댓글의 부모가 올바른지 확인
    }

    // 중복된 verify 호출을 처리하는 메서드
    private void verifyCommonInvocations(Long recordId, int totalInvocations) {
        verify(memberUtil, times(totalInvocations)).getCurrentMember();
        verify(missionRecordRepository, times(totalInvocations)).findById(recordId);
    }

    private void verifyCommonInvocations(Long recordId) {
        verifyCommonInvocations(recordId, 1);
    }

    // Comment 모킹 생성을 처리하는 메서드
    private Comment createMockParentComment(
            Member member, MissionRecord missionRecord, String content) {
        return createMockComment(member, missionRecord, content, null);
    }

    private Comment createMockComment(
            Member member, MissionRecord missionRecord, String content, Comment parent) {
        return fixtureMonkey
                .giveMeBuilder(Comment.class)
                .set("missionRecord", missionRecord)
                .set("writer", member)
                .set("content", content)
                .set("parent", parent)
                .sample();
    }

    // 조회
    @Test
    void 부모_댓글을_조회합니다() {
        // given
        Long recordId = 1L;
        String content = "너무 이쁘자나~";

        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);
        CommentCreateRequest request = CommentCreateRequest.of(content, recordId, null);
        Comment comment = createMockParentComment(member, missionRecord, content); // 부모 댓글 생성

        mockCommonDependencies(member, missionRecord, recordId, comment); // 공통 모의 설정

        // when: 부모 댓글 생성
        CommentCreateResponse response = commentService.createComment(request);

        // 부모 댓글 조회
        when(commentRepository.findById(response.commentId())).thenReturn(Optional.of(comment));

        // 부모 댓글을 포함한 댓글 리스트 생성
        Comment parentComment = commentRepository.findById(response.commentId()).orElse(null);
        List<Comment> comments = List.of(Objects.requireNonNull(parentComment));

        // 부모 댓글을 CommentFindOneResponse로 변환
        List<CommentFindOneResponse> commentResponses =
                comments.stream()
                        .map(
                                comment1 ->
                                        CommentFindOneResponse.of(
                                                comment1.getParent() != null
                                                        ? comment1.getParent().getId()
                                                        : null,
                                                comment1.getId(),
                                                comment1.getContent(),
                                                comment1.getWriter().getId(),
                                                comment1.getWriter().getProfile().getNickname(),
                                                comment1.getWriter()
                                                        .getProfile()
                                                        .getProfileImageUrl(),
                                                comment1.getCreatedAt().toString(),
                                                comment1.getReplyComments().stream()
                                                        .map(
                                                                reply ->
                                                                        CommentFindOneResponse.of(
                                                                                reply.getParent()
                                                                                                != null
                                                                                        ? reply.getParent()
                                                                                                .getId()
                                                                                        : null,
                                                                                reply.getId(),
                                                                                reply.getContent(),
                                                                                reply.getWriter()
                                                                                        .getId(),
                                                                                reply.getWriter()
                                                                                        .getProfile()
                                                                                        .getNickname(),
                                                                                reply.getWriter()
                                                                                        .getProfile()
                                                                                        .getProfileImageUrl(),
                                                                                reply.getCreatedAt()
                                                                                        .toString(),
                                                                                List.of()))
                                                        .collect(Collectors.toList())))
                        .toList();

        // 모의 객체 설정
        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.of(missionRecord));
        when(commentRepository.findAllCommentsByMissionRecord(missionRecord)).thenReturn(comments);

        // when: 댓글 조회
        CommentFindResponse result = commentService.findCommentsByRecordId(recordId);

        // then: 검증
        assertNotNull(result);
        assertEquals(commentResponses.size(), result.comments().size()); // 댓글 개수 비교
        assertEquals(
                commentResponses.get(0).commentId(),
                result.comments().get(0).commentId()); // 첫 번째 댓글 ID 비교

        assertEquals(commentResponses.get(0).content(), content); // 첫 번째 댓글 내용 비교
        assertEquals(result.comments().get(0).content(), content);
        assertEquals(commentResponses.get(0).content(), result.comments().get(0).content());
    }

    @Test
    void 자식_댓글을_조회합니다() {
        // given
        Long recordId = 1L;
        String parentContent = "부모 댓글입니다.";
        String childContentPrefix = "자식 댓글 내용 ";

        // 부모 댓글 생성
        Member member =
                fixtureMonkey
                        .giveMeBuilder(Member.class)
                        .set("status", MemberStatus.NORMAL)
                        .sample();
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);
        Comment parentComment = createMockParentComment(member, missionRecord, parentContent);

        // 자식 댓글 생성
        List<Comment> childComments = new ArrayList<>();
        for (int i = 1; i <= CHILD_COMMENT_COUNT; i++) {
            String childContent = childContentPrefix + i;
            Comment childComment =
                    createMockComment(member, missionRecord, childContent, parentComment);
            childComments.add(childComment);
        }

        // 부모 댓글과 자식 댓글이 포함된 댓글 리스트 생성
        List<Comment> allComments = new ArrayList<>();
        allComments.add(parentComment);
        allComments.addAll(childComments);

        // Mock 설정: 댓글 조회
        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.of(missionRecord));
        when(commentRepository.findAllCommentsByMissionRecord(missionRecord))
                .thenReturn(allComments);

        // when: 댓글 조회 메서드 호출
        CommentFindResponse result = commentService.findCommentsByRecordId(recordId);

        // then: 부모 댓글 검증
        assertNotNull(result);
        assertEquals(1, result.comments().size(), "부모 댓글은 하나만 있어야 합니다.");

        CommentFindOneResponse parentResponse = result.comments().get(0);

        assertEquals(parentComment.getId(), parentResponse.commentId(), "부모 댓글 ID가 일치해야 합니다.");
        assertEquals(parentComment.getContent(), parentResponse.content(), "부모 댓글 내용이 일치해야 합니다.");
        assertEquals(
                parentComment.getWriter().getId(),
                parentResponse.writerId(),
                "부모 댓글 작성자 ID가 일치해야 합니다.");
        assertEquals(
                CHILD_COMMENT_COUNT, parentResponse.replyComments().size(), "자식 댓글의 개수가 일치해야 합니다.");

        // 자식 댓글 검증
        for (int i = 0; i < CHILD_COMMENT_COUNT; i++) {
            CommentFindOneResponse childResponse = parentResponse.replyComments().get(i);
            Comment expectedChildComment = childComments.get(i);

            assertEquals(
                    expectedChildComment.getId(), childResponse.commentId(), "자식 댓글 ID가 일치해야 합니다.");
            assertEquals(
                    expectedChildComment.getContent(),
                    childResponse.content(),
                    "자식 댓글 내용이 일치해야 합니다.");
            assertEquals(
                    expectedChildComment.getWriter().getId(),
                    childResponse.writerId(),
                    "자식 댓글 작성자 ID가 일치해야 합니다.");
            assertEquals(
                    parentComment.getId(), childResponse.parentId(), "자식 댓글의 부모 ID가 일치해야 합니다.");
        }

        // 자식 댓글의 부모가 부모 댓글로 설정되었는지 확인
        assertTrue(
                parentResponse.replyComments().stream()
                        .allMatch(reply -> reply.parentId().equals(parentComment.getId())),
                "모든 자식 댓글의 부모 ID는 부모 댓글 ID와 일치해야 합니다.");
        // 자식 댓글 내용 검증
        assertTrue(
                parentResponse.replyComments().stream()
                        .allMatch(reply -> reply.content().startsWith(childContentPrefix)),
                "모든 자식 댓글의 내용은 '자식 댓글 내용'으로 시작해야 합니다.");
        // assertEquals로 자식 댓글 내용 검증

        for (int i = 0; i < CHILD_COMMENT_COUNT; i++) {
            assertEquals(
                    childContentPrefix + (i + 1),
                    parentResponse.replyComments().get(i).content(),
                    "자식 댓글 내용이 일치해야 합니다.");
        }
    }
}
