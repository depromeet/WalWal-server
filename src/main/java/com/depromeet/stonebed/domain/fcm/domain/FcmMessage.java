package com.depromeet.stonebed.domain.fcm.domain;

public record FcmMessage(String title, String body, String token, String deepLink) {

    public static FcmMessage of(String title, String body, String token, String deepLink) {
        return new FcmMessage(title, body, token, deepLink);
    }
}
