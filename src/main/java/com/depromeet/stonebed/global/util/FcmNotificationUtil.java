package com.depromeet.stonebed.global.util;

import com.google.firebase.messaging.*;

public class FcmNotificationUtil {
    private FcmNotificationUtil() {
        throw new UnsupportedOperationException("인스턴스화 방지");
    }

    public static Notification buildNotification(String title, String body) {
        return Notification.builder().setTitle(title).setBody(body).build();
    }
}
