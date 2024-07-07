package com.depromeet.stonebed.domain.auth.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record IdTokenRequest(
        @Schema(description = "apple idToken", example = "apple_id_token") String idToken) {}
