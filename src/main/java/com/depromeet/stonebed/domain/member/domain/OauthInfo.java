package com.depromeet.stonebed.domain.member.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record OauthInfo(String oauthId, String oauthProvider, String oauthEmail) {
    public static OauthInfo createOauthInfo(
            String oauthId, String oauthProvider, String oauthEmail) {
        return new OauthInfo(oauthId, oauthProvider, oauthEmail);
    }
}
