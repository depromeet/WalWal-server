package com.depromeet.stonebed.infra.properties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties({
    RedisProperties.class,
    S3Properties.class,
    JwtProperties.class,
    AppleProperties.class,
    SwaggerProperties.class,
    SqsProperties.class,
    DiscordProperties.class
})
@Configuration
public class PropertiesConfig {}
