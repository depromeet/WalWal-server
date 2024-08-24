package com.depromeet.stonebed.domain.feed.dao;

import static com.depromeet.stonebed.domain.member.domain.QMember.*;
import static com.depromeet.stonebed.domain.mission.domain.QMission.*;
import static com.depromeet.stonebed.domain.missionHistory.domain.QMissionHistory.*;
import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecord.*;
import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecordBoost.*;

import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FeedRepositoryImpl implements FeedRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    private JPAQuery<FindFeedDto> getFeedBaseQuery(Long missionRecordId, Long memberId) {
        return queryFactory
                .select(
                        Projections.constructor(
                                FindFeedDto.class,
                                mission,
                                missionRecord,
                                member,
                                Expressions.asNumber(
                                                missionRecordBoost.count.sumLong().coalesce(0L))
                                        .as("totalBoostCount")))
                .from(missionRecord)
                .leftJoin(missionRecordBoost)
                .on(missionRecordBoost.missionRecord.eq(missionRecord))
                .leftJoin(member)
                .on(missionRecord.member.eq(member))
                .leftJoin(missionHistory)
                .on(missionRecord.missionHistory.eq(missionHistory))
                .leftJoin(mission)
                .on(missionHistory.mission.eq(mission))
                .where(
                        missionRecord.status.eq(MissionRecordStatus.COMPLETED),
                        ltMissionRecordId(missionRecordId),
                        eqMemberId(memberId))
                .groupBy(missionRecord.id)
                .orderBy(missionRecord.id.desc());
    }

    @Override
    public List<FindFeedDto> getFeedContentsUsingCursor(
            Long missionRecordId, Long memberId, int limit) {
        return getFeedBaseQuery(missionRecordId, memberId).limit(limit).fetch();
    }

    @Override
    public FindFeedDto getNextFeedContent(Long missionRecordId, Long memberId) {
        return getFeedBaseQuery(missionRecordId, memberId).fetchFirst();
    }

    private BooleanExpression ltMissionRecordId(Long missionRecordId) {
        return missionRecordId != null ? missionRecord.id.lt(missionRecordId) : null;
    }

    private BooleanExpression eqMemberId(Long memberId) {
        return memberId != null ? missionRecord.member.id.eq(memberId) : null;
    }
}
