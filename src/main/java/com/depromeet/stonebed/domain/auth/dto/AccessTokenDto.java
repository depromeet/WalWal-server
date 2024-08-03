package com.depromeet.stonebed.domain.auth.dto;

import com.depromeet.stonebed.domain.member.domain.MemberRole;

public record AccessTokenDto(Long memberId, MemberRole memberRole, String tokenValue) {}
