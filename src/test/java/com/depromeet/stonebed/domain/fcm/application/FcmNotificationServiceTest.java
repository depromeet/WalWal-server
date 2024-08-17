package com.depromeet.stonebed.domain.fcm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.fcm.dao.FcmNotificationRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class FcmNotificationServiceTest extends FixtureMonkeySetUp {

    @Mock private FcmService fcmService;
    @Mock private FcmNotificationRepository notificationRepository;
    @Mock private MissionRecordBoostRepository missionRecordBoostRepository;
    @Mock private FcmRepository fcmRepository;
    @Mock private MemberUtil memberUtil;

    @InjectMocks private FcmNotificationService fcmNotificationService;

    @Test
    void 정규알림_응답값_저장() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(memberUtil.getCurrentMember()).thenReturn(member);

        // when
        fcmNotificationService.saveNotification(
                FcmNotificationType.MISSION, "title", "message", "imageUrl", 1L, false);

        // then
        verify(notificationRepository, times(1)).save(any(FcmNotification.class));
    }

    @Test
    void 현재_회원의_알림들_조회() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(memberUtil.getCurrentMember()).thenReturn(member);

        FcmNotification fcmNotification =
                fixtureMonkey.giveMeBuilder(FcmNotification.class).set("member", member).sample();

        when(notificationRepository.findAllByMember(member)).thenReturn(List.of(fcmNotification));

        // when
        List<FcmNotificationResponse> responses =
                fcmNotificationService.getNotificationsForCurrentMember();

        // then
        assertTrue(responses.size() > 0);
        verify(notificationRepository, times(1)).findAllByMember(member);
    }

    @Test
    void 알림을_읽음_처리하면_상태가_변경된다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        FcmNotification fcmNotification =
                fixtureMonkey
                        .giveMeBuilder(FcmNotification.class)
                        .set("member", member)
                        .set("isRead", false)
                        .sample();
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(notificationRepository.findByIdAndMember(1L, member))
                .thenReturn(Optional.of(fcmNotification));

        // when
        fcmNotificationService.markNotificationAsRead(1L);

        // then
        assertTrue(fcmNotification.getIsRead());
        verify(notificationRepository, times(1)).save(fcmNotification);
    }

    @Test
    void 알림이_존재하지_않으면_예외가_발생한다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(notificationRepository.findByIdAndMember(1L, member)).thenReturn(Optional.empty());

        // when
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> fcmNotificationService.markNotificationAsRead(1L));

        // then
        assertTrue(exception.getErrorCode() == ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
