package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.global.util.FcmNotificationUtil;
import com.google.firebase.messaging.Notification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmScheduledService {
    private final FcmService fcmService;
    private final FcmNotificationService fcmNotificationService;
    private final FcmRepository fcmRepository;
    private final MissionRecordRepository missionRecordRepository;

    // 매일 0시 0분에 실행
    @Scheduled(cron = "0 0 0 * * ?")
    public void removeInactiveTokens() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(2);
        List<FcmToken> inactiveTokens = fcmRepository.findAllByUpdatedAtBefore(cutoffDate);
        fcmRepository.deleteAll(inactiveTokens);
        log.info("비활성 토큰 {}개 삭제 완료", inactiveTokens.size());
    }

    // 매일 9시 0분에 실행
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendDailyNotification() {
        String title = "미션 시작!";
        String message = "새로운 미션을 지금 시작해보세요!";
        Notification notification = FcmNotificationUtil.buildNotification(title, message);

        fcmService.sendMulticastMessageToAll(notification);
        log.info("모든 사용자에게 정규 알림 전송 완료");

        fcmNotificationService.saveNotification(
                FcmNotificationType.MISSION, title, message, null, null, false);
    }

    // 매일 18시 0분에 실행
    @Scheduled(cron = "0 0 19 * * ?")
    public void sendReminderToIncompleteMissions() {
        String title = "미션 리마인드";
        String message = "미션 종료까지 5시간 남았어요!";
        Notification notification = FcmNotificationUtil.buildNotification(title, message);

        List<String> tokens = getIncompleteMissionTokens();
        fcmService.sendMulticastMessage(notification, tokens);
        log.info("미완료 미션 사용자에게 리마인더 전송 완료. 총 토큰 수: {}", tokens.size());

        fcmNotificationService.saveNotification(
                FcmNotificationType.MISSION, title, message, null, null, false);
    }

    private List<String> getIncompleteMissionTokens() {
        return missionRecordRepository.findAllByStatus(MissionRecordStatus.NOT_COMPLETED).stream()
                .map(
                        missionRecord -> {
                            FcmToken fcmToken =
                                    fcmRepository
                                            .findByMember(missionRecord.getMember())
                                            .orElse(null);
                            return fcmToken != null ? fcmToken.getToken() : null;
                        })
                .filter(Objects::nonNull)
                .toList();
    }
}
