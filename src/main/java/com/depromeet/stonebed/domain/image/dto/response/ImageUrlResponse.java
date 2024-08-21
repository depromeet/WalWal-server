package com.depromeet.stonebed.domain.image.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record ImageUrlResponse(
        @Schema(description = "이미지 URL", example = "https://default/image/1.jpg") String imageUrl) {
    public static ImageUrlResponse of(String imageUrl) {
        return new ImageUrlResponse(imageUrl);
    }
}
