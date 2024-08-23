package com.depromeet.stonebed.domain.fcm.dao;

import static com.depromeet.stonebed.domain.fcm.domain.QFcmToken.*;
import static com.depromeet.stonebed.domain.member.domain.QMember.*;

import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FcmRepositoryImpl implements FcmRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findAllValidTokens() {
        return queryFactory
                .select(fcmToken.token)
                .from(fcmToken)
                .join(fcmToken.member, member)
                .where(member.status.eq(MemberStatus.NORMAL).and(fcmToken.token.isNotNull()))
                .fetch();
    }
}
