package com.depromeet.stonebed.infra.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "apple")
public record AppleProperties(
        EnvironmentProperties dev, EnvironmentProperties prod, String keyId, String p8) {

    public static record EnvironmentProperties(String clientId, String teamId) {}
}
