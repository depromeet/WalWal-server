// src/test/java/com/depromeet/stonebed/domain/comment/application/CommentServiceTest.java

package com.depromeet.stonebed.domain.comment.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.comment.dao.CommentRepository;
import com.depromeet.stonebed.domain.comment.domain.Comment;
import com.depromeet.stonebed.domain.comment.dto.request.CommentCreateRequest;
import com.depromeet.stonebed.domain.comment.dto.response.CommentCreateResponse;
import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.fcm.dao.FcmTokenRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.Optional;
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

    private Member member;
    private MissionRecord missionRecord;

    @Test
    void 부모_댓글_생성_성공() {
        // given
        Long recordId = 1L;
        Long parentId = null;
        String content = "너무 이쁘자나~";

        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);
        CommentCreateRequest request = CommentCreateRequest.of(content, recordId, parentId);
        Comment comment =
                fixtureMonkey
                        .giveMeBuilder(Comment.class)
                        .set("missionRecord", missionRecord)
                        .set("writer", member)
                        .set("content", content)
                        .sample();

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.of(missionRecord));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);

        // when
        CommentCreateResponse response = commentService.createComment(request);

        // then
        assertNotNull(response);
        assertEquals(comment.getId(), response.commentId());

        verify(memberUtil).getCurrentMember();
        verify(missionRecordRepository).findById(recordId);
        verify(commentRepository).save(any(Comment.class));
    }
}
