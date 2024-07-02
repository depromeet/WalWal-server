package com.depromeet.stonebed.domain.member.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Profile {

    @Schema(description = "닉네임", example = "왈왈멍")
    private String nickname;

    @Schema(description = "프로필 이미지 URL", example = "./profile.jpg")
    private String profileImageUrl;

    @Builder(access = AccessLevel.PRIVATE)
    private Profile(String nickname, String profileImageUrl) {
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
    }

    public static Profile createProfile(String nickname, String profileImageUrl) {
        return Profile.builder().nickname(nickname).profileImageUrl(profileImageUrl).build();
    }
}
