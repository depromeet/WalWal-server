package com.depromeet.stonebed.domain.member.dto.response;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.RaisePet;
import io.swagger.v3.oas.annotations.media.Schema;

public record MemberInfoResponse(
        @Schema(description = "회원 ID", example = "1") Long memberId,
        @Schema(description = "회원 닉네임", example = "닉네임") String nickname,
        @Schema(description = "회원 프로필 이미지 URL", example = "https://example.com/profile.jpg")
                String profileImageUrl,
        @Schema(description = "회원의 반려동물", example = "DOG") RaisePet raisePet) {
    public static MemberInfoResponse from(Member member) {
        return new MemberInfoResponse(
                member.getId(),
                member.getProfile().getNickname(),
                member.getProfile().getProfileImageUrl(),
                member.getRaisePet());
    }
}
