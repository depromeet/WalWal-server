package com.depromeet.stonebed.domain.follow.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

public record FollowCreateRequest(
        @Schema(description = "팔로우 하고자 하는 사용자 아이디", example = "1") Long targetId) {}
