package com.depromeet.stonebed.config;

import com.depromeet.stonebed.infra.config.redis.RedisConfig;
import com.depromeet.stonebed.infra.properties.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;

@TestConfiguration
@EnableConfigurationProperties({RedisProperties.class})
@Import({RedisConfig.class})
public class TestRedisConfig {}
