package com.depromeet.stonebed.global.config.swagger;

import com.depromeet.stonebed.global.common.constants.UrlConstants;
import com.depromeet.stonebed.global.util.SpringEnvironmentUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private static final String SERVER_NAME = "WalWal";
    private static final String GITHUB_URL = "https://github.com/depromeet/WalWal-server";
    private static final String PACKAGES_TO_SCAN = "com.depromeet.stonebed";
    private static final String SWAGGER_API_TITLE = "WalWal 프로젝트 API 문서";
    private static final String SWAGGER_API_DESCRIPTION = "WalWal 프로젝트 API 문서입니다.";

    private final SpringEnvironmentUtil springEnvironmentUtil;

    @Value("${api.version}")
    private String apiVersion;

    // OpenAPI Bean 설정
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .servers(swaggerServers())
                .addSecurityItem(securityRequirement())
                .components(authSetting())
                .info(swaggerInfo());
    }

    private Info swaggerInfo() {
        License license = new License();
        license.setUrl(GITHUB_URL);
        license.setName(SERVER_NAME);

        return new Info()
                .title(SWAGGER_API_TITLE)
                .version("v" + apiVersion)
                .description(SWAGGER_API_DESCRIPTION)
                .license(license);
    }

    private String getServerUrl() {
        return switch (springEnvironmentUtil.getCurrentProfile()) {
            case "prod" -> UrlConstants.PROD_SERVER_URL.getValue();
            case "dev" -> UrlConstants.DEV_SERVER_URL.getValue();
            default -> UrlConstants.LOCAL_SERVER_URL.getValue();
        };
    }

    private List<Server> swaggerServers() {
        Server server = new Server().url(getServerUrl()).description(SWAGGER_API_DESCRIPTION);
        return List.of(server);
    }

    private Components authSetting() {
        return new Components()
                .addSecuritySchemes(
                        "accessToken",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization"));
    }

    // GroupedOpenApi Bean 설정
    @Bean
    public GroupedOpenApi groupedOpenApi() {
        return GroupedOpenApi.builder()
                .group("WalWal API")
                .packagesToScan(PACKAGES_TO_SCAN)
                .build();
    }

    private SecurityRequirement securityRequirement() {
        SecurityRequirement securityRequirement = new SecurityRequirement();
        securityRequirement.addList("accessToken");
        return securityRequirement;
    }

    @Bean
    public ModelResolver modelResolver(ObjectMapper objectMapper) {
        // 객체 직렬화
        // swagger에서는 objectMapper를 사용하기에 objectMapper를 사용할 수 있도록 설정
        return new ModelResolver(objectMapper);
    }
}
