package com.depromeet.stonebed.domain.member.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OauthInfo {

    @Schema(description = "소셜 ID", example = "123487892")
    public String oauthId;

    @Schema(description = "소셜 제공자", example = "APPLE")
    private String oauthProvider;

    @Schema(description = "소셜 이메일", example = "test@gmail.com")
    @Column(unique = true)
    private String oauthEmail;

    @Builder(access = AccessLevel.PRIVATE)
    private OauthInfo(String oauthId, String oauthProvider, String oauthEmail) {
        this.oauthId = oauthId;
        this.oauthProvider = oauthProvider;
        this.oauthEmail = oauthEmail;
    }

    public static OauthInfo createOauthInfo(
            String oauthId, String oauthProvider, String oauthEmail) {
        return OauthInfo.builder()
                .oauthId(oauthId)
                .oauthProvider(oauthProvider)
                .oauthEmail(oauthEmail)
                .build();
    }
}
