package com.depromeet.stonebed.domain.member.dto;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.member.domain.MarketingAgreement;
import com.depromeet.stonebed.domain.member.domain.RaisePet;

public record CreateNewUserDTO(
        OAuthProvider provider,
        String oauthId,
        String nickname,
        RaisePet raisePet,
        String profileImageUrl,
        String email,
        MarketingAgreement marketingAgreement) {
    public static CreateNewUserDTO of(
            OAuthProvider provider,
            String oauthId,
            String nickname,
            RaisePet raisePet,
            String profileImageUrl,
            String email,
            MarketingAgreement marketingAgreement) {
        return new CreateNewUserDTO(
                provider, oauthId, nickname, raisePet, profileImageUrl, email, marketingAgreement);
    }
}
