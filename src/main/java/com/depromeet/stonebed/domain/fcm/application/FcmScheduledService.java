package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
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
        List<FcmToken> inactiveTokens =
                fcmRepository.findAll().stream()
                        .filter(token -> token.getUpdatedAt().isBefore(cutoffDate))
                        .toList();

        for (FcmToken token : inactiveTokens) {
            fcmRepository.delete(token);
        }
    }

    // 매일 12시 0분에 실행
    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyNotification() {
        String title = "정규 메세지 제목!";
        String body = "정규 메세지 내용!";
        fcmService.sendMessageToAll(title, body);
    }

    // 매일 18시 0분에 실행
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendReminderToIncompleteMissions() {
        List<String> tokens = getIncompleteMissionTokens();
        String title = "리마인더 메세지 제목!";
        String body = "리마인더 메세지 내용!";

        for (String token : tokens) {
            try {
                fcmService.sendMessageTo(token, title, body);
            } catch (IOException e) {
                log.error("다음 token이 FCM 리마인더 전송에 실패했습니다: {}", token, e);
            }
        }
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
