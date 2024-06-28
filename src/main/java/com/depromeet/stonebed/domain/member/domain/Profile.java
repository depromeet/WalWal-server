package com.depromeet.stonebed.domain.member.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;

@Embeddable
public record Profile(
        @Schema(description = "닉네임", example = "왈왈멍") String nickname,
        @Schema(description = "프로필 이미지 URL", example = "./profile.jpg") String profileImageUrl) {
    public static Profile createProfile(String nickname, String profileImageUrl) {
        return new Profile(nickname, profileImageUrl);
    }
}
