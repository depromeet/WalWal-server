package com.depromeet.stonebed.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cloud.aws")
public record SqsProperties(String accessKey, String secretKey, String region, String queueUrl) {}
