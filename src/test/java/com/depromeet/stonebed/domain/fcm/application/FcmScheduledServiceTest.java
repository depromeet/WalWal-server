package com.depromeet.stonebed.domain.fcm.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
public class FcmScheduledServiceTest extends FixtureMonkeySetUp {

    @Mock private FcmService fcmService;
    @Mock private FcmNotificationService fcmNotificationService;
    @Mock private FcmRepository fcmRepository;
    @Mock private FcmTokenService fcmTokenService;
    @Mock private MissionRecordRepository missionRecordRepository;

    @InjectMocks private FcmScheduledService fcmScheduledService;

    @Test
    void 비활성화된_토큰을_삭제하면_정상적으로_삭제된다() {
        // given
        List<FcmToken> tokens = fixtureMonkey.giveMe(FcmToken.class, 5);
        when(fcmRepository.findAllByUpdatedAtBefore(any(LocalDateTime.class))).thenReturn(tokens);

        // when
        fcmScheduledService.removeInactiveTokens();

        // then
        ArgumentCaptor<LocalDateTime> captor = ArgumentCaptor.forClass(LocalDateTime.class);
        verify(fcmRepository).findAllByUpdatedAtBefore(captor.capture());

        LocalDateTime actualCutoffDate = captor.getValue();

        assertNotNull(actualCutoffDate);
        assertTrue(actualCutoffDate.isBefore(LocalDateTime.now()));
        verify(fcmRepository).deleteAll(tokens);
    }

    @Test
    void 매일_정기_알림을_모든_사용자에게_전송하고_저장한다() {
        // given
        List<String> tokens = fixtureMonkey.giveMe(String.class, 5);
        when(fcmTokenService.getAllTokens()).thenReturn(tokens);

        // when
        fcmScheduledService.sendDailyNotification();

        // then
        verify(fcmNotificationService, times(1))
                .sendAndNotifications(eq("미션 시작!"), eq("새로운 미션을 지금 시작해보세요!"), eq(tokens));
    }

    @Test
    void 미완료_미션_사용자에게_리마인더를_전송하고_저장한다() {
        // given
        List<MissionRecord> missionRecords = fixtureMonkey.giveMe(MissionRecord.class, 2);
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        when(missionRecordRepository.findAllByCreatedAtBetweenAndStatusNot(
                        startOfDay, endOfDay, MissionRecordStatus.COMPLETED))
                .thenReturn(missionRecords);

        List<String> tokens =
                missionRecords.stream()
                        .map(
                                missionRecord -> {
                                    FcmToken token =
                                            fixtureMonkey
                                                    .giveMeBuilder(FcmToken.class)
                                                    .set("member", missionRecord.getMember())
                                                    .sample();
                                    when(fcmRepository.findByMember(missionRecord.getMember()))
                                            .thenReturn(Optional.of(token));
                                    return token.getToken();
                                })
                        .collect(Collectors.toList());

        // when
        fcmScheduledService.sendReminderToIncompleteMissions();

        // then
        verify(fcmNotificationService, times(1))
                .sendAndNotifications(eq("미션 리마인드"), eq("미션 종료까지 5시간 남았어요!"), eq(tokens));
    }
}
