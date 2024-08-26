package com.depromeet.stonebed.domain.member.dao;

public interface MemberRepositoryCustom {

    boolean existsByProfileNickname(String nickname, String currentNickname);
}
