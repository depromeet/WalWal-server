package com.depromeet.stonebed;

import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class StonebedApplicationTests {
    @MockBean private FirebaseMessaging firebaseMessaging;
    @MockBean private FcmNotificationService fcmNotificationService;

    @Test
    void contextLoads() {}
}
