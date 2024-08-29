package com.depromeet.stonebed.global.config.sqs;

import com.depromeet.stonebed.infra.properties.SqsProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@RequiredArgsConstructor
public class SqsConfig {

    private final SqsProperties sqsProperties;

    @Bean
    public SqsClient sqsClient() {
        AwsBasicCredentials awsCreds =
                AwsBasicCredentials.create(sqsProperties.accessKey(), sqsProperties.secretKey());

        return SqsClient.builder()
                .region(Region.of(sqsProperties.region()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .build();
    }
}
