package com.depromeet.stonebed.domain.member.dto;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.member.domain.RaisePet;

public record CreateNewUserDTO(
        OAuthProvider provider,
        String oauthId,
        String nickname,
        RaisePet raisePet,
        String profileImageUrl,
        String email) {
    public static CreateNewUserDTO of(
            OAuthProvider provider,
            String oauthId,
            String nickname,
            RaisePet raisePet,
            String profileImageUrl,
            String email) {
        return new CreateNewUserDTO(provider, oauthId, nickname, raisePet, profileImageUrl, email);
    }
}
