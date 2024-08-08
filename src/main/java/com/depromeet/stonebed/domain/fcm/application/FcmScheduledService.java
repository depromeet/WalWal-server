package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.global.util.FcmNotificationUtil;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmScheduledService {
    private final FcmService fcmService;
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

    // 매일 12시 0분에 실행
    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyNotification() {
        Notification notification = FcmNotificationUtil.buildNotification("정규 메세지 제목", "정규 메세지 내용");
        fcmService.sendMulticastMessageToAll(notification);
        log.info("모든 사용자에게 정규 알림 전송 완료");
    }

    // 매일 18시 0분에 실행
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendReminderToIncompleteMissions() throws IOException {
        Notification notification =
                FcmNotificationUtil.buildNotification("리마인드 메세지 제목", "리마인드 메세지 내용");
        List<String> tokens = getIncompleteMissionTokens();
        fcmService.sendMulticastMessage(notification, tokens);
        log.info("미완료 미션 사용자에게 리마인더 전송 완료. 총 토큰 수: {}", tokens.size());
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
                .filter(token -> token != null)
                .toList();
    }
}
