package com.depromeet.stonebed.domain.member.dto.request;

import com.depromeet.stonebed.domain.member.domain.RaisePet;

public record CreateMemberRequest(String nickname, RaisePet raisePet) {}
