package com.depromeet.stonebed.domain.follow.dto.response;

public record FollowedMemberResponse(Long memberId, String nickname, String profileImageUrl) {
    public static FollowedMemberResponse of(
            Long memberId, String nickname, String profileImageUrl) {
        return new FollowedMemberResponse(memberId, nickname, profileImageUrl);
    }
}
