package com.depromeet.stonebed;

import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.sqs.application.SqsMessageService;
import com.google.firebase.messaging.FirebaseMessaging;
import io.awspring.cloud.autoconfigure.sqs.SqsAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import software.amazon.awssdk.services.sqs.SqsClient;

@EnableAutoConfiguration(exclude = {SqsAutoConfiguration.class})
@SpringBootTest
@ActiveProfiles("test")
class StonebedApplicationTests {
    @MockBean private FirebaseMessaging firebaseMessaging;
    @MockBean private FcmNotificationService fcmNotificationService;
    @MockBean private SqsMessageService sqsMessageService;
    @MockBean private SqsClient sqsClient;

    @Test
    void contextLoads() {}
}
