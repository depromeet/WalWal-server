package com.depromeet.stonebed.domain.member.dao;

import static com.depromeet.stonebed.domain.member.domain.QMember.*;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public boolean existsByProfileNickname(String nickname) {
        // 존재하는 경우의 쿼리
        return jpaQueryFactory
                        .selectOne()
                        .from(member)
                        .where(isProfileNickname(nickname).and(isNotEmptyProfileNickname()))
                        .fetchFirst()
                != null;
    }

    private BooleanExpression isProfileNickname(String nickname) {
        return member.profile.nickname.eq(nickname);
    }

    private BooleanExpression isNotEmptyProfileNickname() {
        return member.profile.nickname.ne("");
    }
}
