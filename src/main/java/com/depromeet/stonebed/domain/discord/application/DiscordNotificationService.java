package com.depromeet.stonebed.domain.discord.application;

import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.infra.properties.DiscordProperties;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class DiscordNotificationService {

    private final DiscordProperties discordProperties;
    private final RestClient restClient;

    public void sendDiscordMessage(String message) {
        Map<String, String> payload = new HashMap<>();
        payload.put("content", message);

        try {
            String discordWebhookUrl = discordProperties.url();
            log.info("Sending Discord notification to URL: {}", discordWebhookUrl);

            restClient
                    .post()
                    .uri(discordWebhookUrl)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(payload)
                    .exchange(
                            (request, response) -> {
                                if (!response.getStatusCode().is2xxSuccessful()) {
                                    throw new CustomException(
                                            ErrorCode.DISCORD_NOTIFICATION_FAILED);
                                }
                                log.info("Discord 알림 전송 성공: {}", message);
                                return response.bodyTo(String.class);
                            });

        } catch (Exception e) {
            log.error("Discord 알림 전송 중 예외 발생: {}", message, e);
        }
    }
}
