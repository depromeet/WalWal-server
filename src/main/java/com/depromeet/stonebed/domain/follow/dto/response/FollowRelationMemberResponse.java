package com.depromeet.stonebed.domain.follow.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record FollowRelationMemberResponse(
        @Schema(description = "팔로우 상태", defaultValue = "NOT_FOLLOWING") FollowStatus followStatus) {
    public static FollowRelationMemberResponse from(FollowStatus followStatus) {
        return new FollowRelationMemberResponse(followStatus);
    }
    ;
}
