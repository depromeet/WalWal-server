package com.depromeet.stonebed.domain.sqs.application;

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

    private final SqsClient sqsClient;

    private final SqsProperties sqsProperties;

    private final ObjectMapper objectMapper;

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

    public void sendBatchMessages(List<String> tokens, String title, String message) {
        List<SendMessageBatchRequestEntry> entries = new ArrayList<>();
        for (String token : tokens) {
            try {
                FcmMessage fcmMessage = new FcmMessage(title, message, token);
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

        SendMessageBatchRequest batchRequest =
                SendMessageBatchRequest.builder()
                        .queueUrl(sqsProperties.queueUrl())
                        .entries(entries)
                        .build();

        try {
            SendMessageBatchResponse batchResponse = sqsClient.sendMessageBatch(batchRequest);

            // 실패한 메시지 처리
            List<BatchResultErrorEntry> failedMessages = batchResponse.failed();
            if (!failedMessages.isEmpty()) {
                for (BatchResultErrorEntry failed : failedMessages) {
                    log.error("메시지 전송 실패, ID {}: {}", failed.id(), failed.message());
                }
            }

        } catch (Exception e) {
            log.error("SQS 배치 메시지 전송 실패: {}", e.getMessage());
        }
    }
}
