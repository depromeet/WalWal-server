package com.depromeet.stonebed.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberProfileUpdateRequest(
        @Schema(description = "변경할 닉네임", example = "왈왈멍") String nickname,
        @Schema(description = "변경할 프로필 이미지 URL", example = "https://example.com/profile.jpg")
                String profileImageUrl) {}
