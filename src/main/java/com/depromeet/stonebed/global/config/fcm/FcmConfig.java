package com.depromeet.stonebed.global.config.fcm;

import com.depromeet.stonebed.infra.properties.FcmProperties;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class FcmConfig {

    private final FcmProperties fcmProperties;

    @Bean
    public FirebaseMessaging firebaseMessaging() throws IOException {
        String firebaseCredential = fcmProperties.credential();

        if (firebaseCredential == null || firebaseCredential.isEmpty()) {
            throw new IllegalStateException("FCM_CREDENTIAL environment variable is not set.");
        }

        ByteArrayInputStream credentials =
                new ByteArrayInputStream(firebaseCredential.getBytes(StandardCharsets.UTF_8));
        FirebaseOptions options =
                FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(credentials))
                        .build();

        FirebaseApp firebaseApp = FirebaseApp.initializeApp(options);
        return FirebaseMessaging.getInstance(firebaseApp);
    }
}
