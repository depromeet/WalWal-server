package com.depromeet.stonebed.domain.follow.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FollowStatus {
    FOLLOWING("팔로잉"), // A가 이미 B를 팔로우 중일때 (팔로우 취소)
    NOT_FOLLOWING("팔로우"), // A가 B를 팔로우하지 않고, B도 A를 팔로우하지 않을 때 (팔로우 추가)
    ;

    private final String value;
}
