package com.depromeet.stonebed.domain.image.dto.request;

import com.depromeet.stonebed.domain.image.domain.ImageFileExtension;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberProfileImageUploadCompleteRequest(
        @Schema(description = "이미지 파일의 확장자", defaultValue = "JPEG")
                ImageFileExtension imageFileExtension,
        @Schema(description = "닉네임", defaultValue = "수정닉네임") String nickname) {}
