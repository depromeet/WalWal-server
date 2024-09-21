package com.depromeet.stonebed.domain.fcm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.fcm.dao.FcmTokenRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.scheduler.fcm.FcmScheduler;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
public class FcmSchedulerTest extends FixtureMonkeySetUp {
    @Mock private FcmNotificationService fcmNotificationService;
    @Mock private FcmTokenRepository fcmTokenRepository;
    @Mock private MissionRecordRepository missionRecordRepository;

    @InjectMocks private FcmScheduler fcmScheduler;

    @Test
    void 비활성화된_토큰을_삭제하면_정상적으로_삭제된다() {
        // given
        List<FcmToken> tokens = fixtureMonkey.giveMe(FcmToken.class, 5);
        when(fcmTokenRepository.findAllByUpdatedAtBefore(any(LocalDateTime.class)))
                .thenReturn(tokens);

        // when
        fcmScheduler.removeInactiveTokens();

        // then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(fcmTokenRepository).findAllByUpdatedAtBefore(captor.capture());

        LocalDateTime actualCutoffDate = captor.getValue();

        assertNotNull(actualCutoffDate);
        assertTrue(actualCutoffDate.isBefore(LocalDateTime.now()));
        verify(fcmTokenRepository).deleteAll(tokens);
    }

    @Test
    void 매일_정기_알림을_모든_사용자에게_전송하고_저장한다() {
        // given
        List<String> tokens = fixtureMonkey.giveMe(String.class, 5);
        when(fcmNotificationService.getAllTokens()).thenReturn(tokens);

        // when
        fcmScheduler.sendDailyNotification();

        // then
        verify(fcmNotificationService, times(1))
                .sendAndNotifications(eq("미션 시작!"), eq("새로운 미션을 지금 시작해보세요!"), eq(tokens));
    }

    @Test
    void 미완료_미션_사용자에게_리마인더를_전송하고_저장한다() {
        // given
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Long> completedMemberIds = List.of(1L, 2L);
        when(missionRecordRepository.findAllByCreatedAtBetweenAndStatus(
                        startOfDay, endOfDay, MissionRecordStatus.COMPLETED))
                .thenReturn(
                        completedMemberIds.stream()
                                .map(
                                        id -> {
                                            MissionRecord record =
                                                    fixtureMonkey
                                                            .giveMeBuilder(MissionRecord.class)
                                                            .set("member.id", id)
                                                            .sample();
                                            record.getMember().updateStatus(MemberStatus.NORMAL);
                                            return record;
                                        })
                                .collect(Collectors.toList()));

        List<FcmToken> allTokens = fixtureMonkey.giveMe(FcmToken.class, 5);
        when(fcmTokenRepository.findAllByMemberStatus(MemberStatus.NORMAL)).thenReturn(allTokens);

        List<String> tokens =
                allTokens.stream()
                        .filter(token -> !completedMemberIds.contains(token.getMember().getId()))
                        .map(FcmToken::getToken)
                        .collect(Collectors.toList());

        // when
        fcmScheduler.sendReminderToIncompleteMissions();

        // then
        verify(fcmNotificationService, times(1))
                .sendAndNotifications(eq("미션 리마인드"), eq("미션 종료까지 5시간 남았어요!"), eq(tokens));
    }
}
