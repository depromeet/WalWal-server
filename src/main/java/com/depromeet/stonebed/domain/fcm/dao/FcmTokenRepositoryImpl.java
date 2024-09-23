package com.depromeet.stonebed.domain.fcm.dao;

import static com.depromeet.stonebed.domain.fcm.domain.QFcmToken.*;
import static com.depromeet.stonebed.domain.member.domain.QMember.*;

import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FcmTokenRepositoryImpl implements FcmTokenRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<String> findAllValidTokens() {
        return jpaQueryFactory
                .select(fcmToken.token)
                .from(fcmToken)
                .join(fcmToken.member, member)
                .where(isMemberStatusNormal(), isTokenNotNull())
                .fetch();
    }

    private BooleanExpression isMemberStatusNormal() {
        return member.status.eq(MemberStatus.NORMAL);
    }

    private BooleanExpression isTokenNotNull() {
        return fcmToken.token.isNotNull();
    }
}
