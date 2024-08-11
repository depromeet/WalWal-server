package com.depromeet.stonebed.domain.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record NicknameCheckRequest(
        @Schema(description = "검사할 닉네임", example = "왈왈멍") String nickname) {
    public static NicknameCheckRequest of(String nickname) {
        return new NicknameCheckRequest(nickname);
    }
}
