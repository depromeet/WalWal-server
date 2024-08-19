package com.depromeet.stonebed.domain.feed.dao;

import static com.depromeet.stonebed.domain.member.domain.QMember.member;
import static com.depromeet.stonebed.domain.mission.domain.QMission.mission;
import static com.depromeet.stonebed.domain.mission.domain.QMissionHistory.missionHistory;
import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecord.missionRecord;
import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecordBoost.missionRecordBoost;

import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import com.querydsl.core.types.Projections;
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

    private JPAQuery<FindFeedDto> getFeedBaseQuery() {
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
                .on(missionHistory.mission.eq(mission));
    }

    @Override
    public List<FindFeedDto> getFeedContentsUsingCursor(
            Long missionRecordId, Long memberId, int limit) {
        return getFeedBaseQuery()
                .where(missionRecord.id.lt(missionRecordId))
                .groupBy(missionRecord.id, member.id, mission.id, missionHistory.id)
                .orderBy(missionRecord.id.desc())
                .limit(limit)
                .fetch();
    }

    @Override
    public List<FindFeedDto> getFeedContents(Long memberId, int limit) {
        return getFeedBaseQuery()
                .groupBy(missionRecord.id, member.id, mission.id, missionHistory.id)
                .orderBy(missionRecord.id.desc())
                .limit(limit)
                .fetch();
    }
}
