package com.depromeet.stonebed.global.common.constants;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UrlConstants {
    PROD_SERVER_URL("https://api.walwal.life"),
    DEV_SERVER_URL("https://dev-api.walwal.life"),
    LOCAL_SERVER_URL("http://localhost:8080"),
    IMAGE_DOMAIN_URL("https://image.walwal.life"),
    ;

    private final String value;
}
