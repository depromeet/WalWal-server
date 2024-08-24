package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.domain.FcmResponseErrorType;
import com.google.firebase.messaging.*;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {
    private static final int BATCH_SIZE = 500;

    private final FcmTokenService fcmTokenService;

    @Transactional(readOnly = true)
    public void sendMulticastMessage(Notification notification, List<String> tokens) {
        int totalTokens = tokens.size();

        for (int i = 0; i < totalTokens; i += BATCH_SIZE) {
            List<String> batchTokens = tokens.subList(i, Math.min(i + BATCH_SIZE, totalTokens));
            MulticastMessage message = buildMulticastMessage(notification, batchTokens);
            sendMessage(message, batchTokens);
        }

        log.info("전체 메세지를 일괄 전송했습니다. 총 메세지 수: {}", totalTokens);
    }

    private MulticastMessage buildMulticastMessage(Notification notification, List<String> tokens) {
        HashMap<String, String> data = new HashMap<>();
        data.put("time", LocalDateTime.now().toString());

        return MulticastMessage.builder()
                .putAllData(data)
                .setNotification(notification)
                .addAllTokens(tokens)
                .build();
    }

    private void sendMessage(MulticastMessage message, List<String> tokens) {
        try {
            BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            handleBatchResponse(response, tokens);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송에 실패했습니다: ", e);
        }
    }

    private void sendMessage(Message message) {
        try {
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("성공적으로 메시지를 전송했습니다. 메시지 ID: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송에 실패했습니다: ", e);
        }
    }

    public void sendSingleMessage(Notification notification, String token) {
        Message message = buildSingleMessage(notification, token);
        sendMessage(message);
    }

    private Message buildSingleMessage(Notification notification, String token) {
        HashMap<String, String> data = new HashMap<>();

        return Message.builder()
                .putAllData(data)
                .setNotification(notification)
                .setToken(token)
                .build();
    }

    private void handleBatchResponse(BatchResponse response, List<String> tokens) {
        response.getResponses().stream()
                .filter(sendResponse -> !sendResponse.isSuccessful())
                .forEach(
                        sendResponse -> {
                            String token =
                                    tokens.get(response.getResponses().indexOf(sendResponse));
                            if (isInvalidOrNotRegistered(sendResponse)) {
                                fcmTokenService.invalidateToken(token);
                                log.warn("FCM 토큰 {}이(가) 유효하지 않거나 등록되지 않았습니다. 토큰을 무효화합니다.", token);
                            }
                        });
    }

    private boolean isInvalidOrNotRegistered(SendResponse sendResponse) {
        String errorMessage = sendResponse.getException().getMessage();
        return FcmResponseErrorType.contains(errorMessage, FcmResponseErrorType.NOT_REGISTERED)
                || FcmResponseErrorType.contains(
                        errorMessage, FcmResponseErrorType.INVALID_REGISTRATION);
    }
}
