package com.depromeet.stonebed.domain.fcm.dto.request;

import lombok.Builder;

@Builder
public record FcmMessageRequest(boolean validateOnly, Message message) {
    @Builder
    public static record Message(Notification notification, String token) {}

    @Builder
    public static record Notification(String title, String body, String image) {}
}
