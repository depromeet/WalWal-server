package com.depromeet.stonebed.domain.sqs.application;

import static com.depromeet.stonebed.global.common.constants.NotificationConstants.*;

import com.depromeet.stonebed.domain.fcm.dao.FcmTokenRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmMessage;
import com.depromeet.stonebed.infra.properties.SqsProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.BatchResultErrorEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry;
import software.amazon.awssdk.services.sqs.model.SendMessageBatchResponse;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageResponse;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class SqsMessageService {
    private final SqsProperties sqsProperties;
    private final ObjectMapper objectMapper;
    private final FcmTokenRepository fcmTokenRepository;
    private final SqsClient sqsClient;

    public void sendMessage(Object message) {
        try {
            String messageBody = objectMapper.writeValueAsString(message);
            SendMessageRequest sendMsgRequest =
                    SendMessageRequest.builder()
                            .queueUrl(sqsProperties.queueUrl())
                            .messageBody(messageBody)
                            .build();

            SendMessageResponse sendMsgResponse = sqsClient.sendMessage(sendMsgRequest);
            log.info("메시지 전송 완료, ID: {}", sendMsgResponse.messageId());
        } catch (Exception e) {
            log.error("SQS 메시지 전송 실패: {}", e.getMessage());
        }
    }

    public void sendBatchMessages(
            List<String> tokens, String title, String message, String deepLink) {

        List<String> failedTokens = new ArrayList<>();

        // 토큰 리스트를 10개씩 분할하여 처리
        for (int i = 0; i < tokens.size(); i += SQS_BATCH_SIZE) {
            List<String> batchTokens =
                    tokens.subList(i, Math.min(i + SQS_BATCH_SIZE, tokens.size()));

            List<SendMessageBatchRequestEntry> entries =
                    createBatchEntries(batchTokens, title, message, deepLink);

            if (!entries.isEmpty()) {
                sendBatchRequest(entries, failedTokens);
            }
        }

        // 실패한 토큰 삭제 처리
        deleteFailedTokens(failedTokens);
    }

    private List<SendMessageBatchRequestEntry> createBatchEntries(
            List<String> batchTokens, String title, String message, String deepLink) {

        List<SendMessageBatchRequestEntry> entries = new ArrayList<>();

        for (String token : batchTokens) {
            try {
                FcmMessage fcmMessage = FcmMessage.of(title, message, token, deepLink);
                String messageBody = objectMapper.writeValueAsString(fcmMessage);
                SendMessageBatchRequestEntry entry =
                        SendMessageBatchRequestEntry.builder()
                                .id(UUID.randomUUID().toString())
                                .messageBody(messageBody)
                                .build();
                entries.add(entry);
            } catch (Exception e) {
                log.error("메시지 직렬화 실패: {}", e.getMessage());
            }
        }

        return entries;
    }

    private void sendBatchRequest(
            List<SendMessageBatchRequestEntry> entries, List<String> failedTokens) {
        SendMessageBatchRequest batchRequest =
                SendMessageBatchRequest.builder()
                        .queueUrl(sqsProperties.queueUrl())
                        .entries(entries)
                        .build();

        try {
            SendMessageBatchResponse batchResponse = sqsClient.sendMessageBatch(batchRequest);
            log.info("배치 메시지 전송 응답: {}", batchResponse);

            // 실패한 메시지 처리
            handleFailedMessages(batchResponse, failedTokens);

        } catch (Exception e) {
            log.error("SQS 배치 메시지 전송 실패: {}", e.getMessage());
        }
    }

    private void handleFailedMessages(
            SendMessageBatchResponse batchResponse, List<String> failedTokens) {
        List<BatchResultErrorEntry> failedMessages = batchResponse.failed();
        for (BatchResultErrorEntry failed : failedMessages) {
            log.error("메시지 전송 실패, ID {}: {}", failed.id(), failed.message());
            failedTokens.add(failed.id());
        }
    }

    private void deleteFailedTokens(List<String> failedTokens) {
        for (String failedToken : failedTokens) {
            fcmTokenRepository.deleteByToken(failedToken);
            log.info("비활성화된 FCM 토큰 삭제: {}", failedToken);
        }
    }
}
