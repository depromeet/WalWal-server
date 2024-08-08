package com.depromeet.stonebed.global.util;

import com.google.firebase.messaging.*;

public class FcmNotificationUtil {
    public static Notification buildNotification(String title, String body) {
        return Notification.builder().setTitle(title).setBody(body).build();
    }
}
