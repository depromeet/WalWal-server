package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmResponseErrorType;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.google.firebase.messaging.*;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FcmService {
    private static final int BATCH_SIZE = 500;

    private final FcmRepository fcmRepository;
    private final MemberUtil memberUtil;

    @Transactional(readOnly = true)
    public void sendMulticastMessageToAll(Notification notification) {
        List<String> tokens = getAllTokens();
        int totalTokens = tokens.size();

        for (int i = 0; i < totalTokens; i += BATCH_SIZE) {
            List<String> batchTokens = tokens.subList(i, Math.min(i + BATCH_SIZE, totalTokens));
            MulticastMessage message = buildMulticastMessage(notification, batchTokens);
            sendMessage(message, batchTokens);
        }
        log.info("전체 메세지를 일괄 전송했습니다. 총 메세지 수: {}", totalTokens);
    }

    @Transactional
    public void sendMulticastMessage(Notification notification, List<String> tokens)
            throws IOException {
        int totalTokens = tokens.size();

        for (int i = 0; i < totalTokens; i += BATCH_SIZE) {
            List<String> batchTokens = tokens.subList(i, Math.min(i + BATCH_SIZE, totalTokens));
            MulticastMessage message = buildMulticastMessage(notification, batchTokens);
            sendMessage(message, batchTokens);
        }
        log.info("리마인드 메세지를 일괄 전송했습니다. 총 메세지 수: {}", totalTokens);
    }

    private MulticastMessage buildMulticastMessage(Notification notification, List<String> tokens) {
        return MulticastMessage.builder()
                .putAllData(
                        new HashMap<>() {
                            {
                                put("time", LocalDateTime.now().toString());
                            }
                        })
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

    private void handleBatchResponse(BatchResponse response, List<String> tokens) {
        response.getResponses().stream()
                .filter(sendResponse -> !sendResponse.isSuccessful())
                .forEach(
                        sendResponse -> {
                            String token =
                                    tokens.get(response.getResponses().indexOf(sendResponse));
                            if (isInvalidOrNotRegistered(sendResponse)) {
                                invalidateToken(token);
                            }
                        });
    }

    private boolean isInvalidOrNotRegistered(SendResponse sendResponse) {
        String errorMessage = sendResponse.getException().getMessage();
        return FcmResponseErrorType.contains(errorMessage, FcmResponseErrorType.NOT_REGISTERED)
                || FcmResponseErrorType.contains(
                        errorMessage, FcmResponseErrorType.INVALID_REGISTRATION);
    }

    @Transactional
    public void invalidateToken(String token) {
        fcmRepository
                .findByToken(token)
                .ifPresentOrElse(
                        fcmToken -> updateToken(fcmToken, ""),
                        () -> {
                            throw new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN);
                        });
    }

    private void updateToken(FcmToken fcmToken, String token) {
        fcmToken.updateToken(token);
        fcmRepository.save(fcmToken);
    }

    @Transactional(readOnly = true)
    public List<String> getAllTokens() {
        return fcmRepository.findAll().stream()
                .map(FcmToken::getToken)
                .filter(token -> !token.isEmpty())
                .toList();
    }

    @Transactional
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

    @Transactional
    public void refreshTokenTimestampForCurrentUser() {
        Member member = memberUtil.getCurrentMember();
        fcmRepository
                .findByMember(member)
                .ifPresentOrElse(
                        fcmToken -> updateToken(fcmToken, fcmToken.getToken()),
                        () -> {
                            throw new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN);
                        });
    }
}
