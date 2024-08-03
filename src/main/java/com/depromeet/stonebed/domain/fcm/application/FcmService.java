package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.fcm.dto.request.FcmMessageRequest;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {
    private final FcmRepository fcmRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MemberUtil memberUtil;

    public void sendMessageToAll(String title, String body) {
        List<String> tokens = getAllTokens();
        for (String token : tokens) {
            try {
                sendMessageTo(token, title, body);
            } catch (IOException e) {
                // 여기서는 개별적으로 오류가 발생해도 전체 전송이 중단되지 않도록 처리합니다.
                log.error("다음 token이 FCM 메세지 전송에 실패했습니다: {}", token, e);
            }
        }
    }

    public void sendMessageTo(String token, String title, String body) throws IOException {
        String message = createFcmMessage(token, title, body);
        RestTemplate restTemplate = new RestTemplate();
        restTemplate
                .getMessageConverters()
                .add(0, new StringHttpMessageConverter(StandardCharsets.UTF_8));

        HttpHeaders headers = createFcmHeaders();
        HttpEntity<String> entity = new HttpEntity<>(message, headers);

        String API_URL = "https://fcm.googleapis.com/v1/projects/walwal-dev-fad47/messages:send";
        ResponseEntity<String> response =
                restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);

        if (response.getStatusCode() != HttpStatus.OK) {
            String responseBody = response.getBody();
            if (responseBody != null
                    && (responseBody.contains("NotRegistered")
                            || responseBody.contains("InvalidRegistration"))) {
                deleteTokenByToken(token);
            }
            throw new CustomException(ErrorCode.FAILED_TO_SEND_FCM_MESSAGE);
        }
    }

    private HttpHeaders createFcmHeaders() throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + getAccessToken());
        return headers;
    }

    private String getAccessToken() throws IOException {
        String firebaseConfigPath = "firebase/walwal-dev-firebase.json";

        GoogleCredentials googleCredentials =
                GoogleCredentials.fromStream(
                                new ClassPathResource(firebaseConfigPath).getInputStream())
                        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

        googleCredentials.refreshIfExpired();
        return googleCredentials.getAccessToken().getTokenValue();
    }

    private String createFcmMessage(String token, String title, String body)
            throws JsonProcessingException {
        ObjectMapper om = new ObjectMapper();
        FcmMessageRequest fcmMessageRequest =
                FcmMessageRequest.builder()
                        .message(
                                FcmMessageRequest.Message.builder()
                                        .token(token)
                                        .notification(
                                                FcmMessageRequest.Notification.builder()
                                                        .title(title)
                                                        .body(body)
                                                        .image(null)
                                                        .build())
                                        .build())
                        .validateOnly(false)
                        .build();

        return om.writeValueAsString(fcmMessageRequest);
    }

    public void storeOrUpdateToken(String token) {
        final Member member = memberUtil.getCurrentMember();
        Optional<FcmToken> existingToken = fcmRepository.findByMember(member);
        existingToken.ifPresentOrElse(
                fcmToken -> {
                    fcmToken.updateToken(token);
                    fcmRepository.save(fcmToken);
                },
                () -> {
                    FcmToken fcmToken = new FcmToken(member, token);
                    fcmRepository.save(fcmToken);
                });
    }

    public void refreshTokenTimestamp(String token) {
        Optional<FcmToken> existingToken = fcmRepository.findByToken(token);
        existingToken.ifPresentOrElse(
                fcmToken -> {
                    fcmToken.updateToken(token);
                    fcmRepository.save(fcmToken);
                },
                () -> {
                    throw new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN);
                });
    }

    public void deleteToken() {
        final Member member = memberUtil.getCurrentMember();
        fcmRepository.deleteByMember(member);
    }

    public void deleteTokenByToken(String token) {
        fcmRepository.deleteByToken(token);
    }

    public List<String> getAllTokens() {
        return fcmRepository.findAll().stream().map(FcmToken::getToken).toList();
    }

    public List<String> getIncompleteMissionTokens() {
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

    // 매월 1일에 실행
    @Scheduled(cron = "0 0 0 1 * ?")
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

    // 매일 오후 12시에 전체 알림 전송
    @Scheduled(cron = "0 0 12 * * ?")
    public void sendDailyNotification() {
        String title = "정규 메세지 제목!";
        String body = "정규 메세지 내용!";
        sendMessageToAll(title, body);
    }

    // 매일 오후 6시에 미션 레코드가 COMPLETED가 아닌 사람들에게 알림 전송
    @Scheduled(cron = "0 0 18 * * ?")
    public void sendReminderToIncompleteMissions() {
        List<String> tokens = getIncompleteMissionTokens();
        String title = "리마인더 메세지 제목!";
        String body = "리마인더 메세지 내용!";

        for (String token : tokens) {
            try {
                sendMessageTo(token, title, body);
            } catch (IOException e) {
                log.error("다음 token이 FCM 리마인더 전송에 실패했습니다: {}", token, e);
            }
        }
    }
}
