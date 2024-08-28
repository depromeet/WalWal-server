package com.depromeet.stonebed.domain.fcm.domain;

public record FcmMessage(String title, String body, String token) {

    public static FcmMessage of(String title, String body, String token) {
        return new FcmMessage(title, body, token);
    }
}
