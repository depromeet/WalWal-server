package com.depromeet.stonebed.domain.member.dto.request;

import com.depromeet.stonebed.domain.member.domain.RaisePet;
import io.swagger.v3.oas.annotations.media.Schema;

public record CreateMemberRequest(
        @Schema(description = "닉네임", example = "왈왈멍") String nickname,
        @Schema(description = "반려동물", example = "DOG") RaisePet raisePet) {}
