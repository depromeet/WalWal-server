package com.depromeet.stonebed.domain.image.dto.request;

import com.depromeet.stonebed.domain.image.domain.ImageFileExtension;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MissionRecordImageUploadRequest(
        @Schema(description = "이미지 파일의 확장자", defaultValue = "JPEG")
                ImageFileExtension imageFileExtension,
        @NotNull @Schema(description = "기록 아이디", defaultValue = "1") Long recordId) {}
