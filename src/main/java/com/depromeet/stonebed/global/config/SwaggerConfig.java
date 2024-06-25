package com.depromeet.stonebed.global.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class SwaggerConfig {
	private static final String PACKAGES_TO_SCAN = "com.depromeet.stonebed";

	@Value("${api.version}")
	private String apiVersion;

	// OpenAPI Bean 설정
	@Bean
	public OpenAPI openAPI() {
		return new OpenAPI()
			.info(new Info()
				.title("WalWal 프로젝트 API 문서화")
				.version(apiVersion)
				.description("WalWal 프로젝트의 Swagger UI 화면입니다."));
	}

	// GroupedOpenApi Bean 설정
	@Bean
	public GroupedOpenApi groupedOpenApi() {
		return GroupedOpenApi.builder().group("WalWal API")
			.packagesToScan(PACKAGES_TO_SCAN)
			.build();
	}
}
