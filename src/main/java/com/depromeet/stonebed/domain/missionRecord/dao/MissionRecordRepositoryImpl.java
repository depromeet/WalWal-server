package com.depromeet.stonebed.domain.missionRecord.dao;

import static com.depromeet.stonebed.domain.mission.domain.QMission.*;
import static com.depromeet.stonebed.domain.missionHistory.domain.QMissionHistory.*;
import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecord.missionRecord;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordDisplay;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRecordRepositoryImpl implements MissionRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MissionRecord> findByMemberIdWithPagination(
            Long memberId, List<MissionRecordDisplay> displays, Pageable pageable) {
        return queryFactory
                .selectFrom(missionRecord)
                .where(isMemberId(memberId).and(inDisplays(displays)))
                .orderBy(missionRecord.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<MissionRecord> findByMemberIdAndCreatedAtFromWithPagination(
            Long memberId,
            LocalDateTime createdAt,
            List<MissionRecordDisplay> displays,
            Pageable pageable) {
        return queryFactory
                .selectFrom(missionRecord)
                .where(
                        isMemberId(memberId)
                                .and(createdAtFrom(createdAt))
                                .and(isCompleted())
                                .and(inDisplays(displays)))
                .orderBy(missionRecord.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public void updateExpiredMissionsToNotCompleted(LocalDateTime currentTime) {
        queryFactory
                .update(missionRecord)
                .set(missionRecord.status, MissionRecordStatus.NOT_COMPLETED)
                .set(missionRecord.updatedAt, LocalDateTime.now())
                .where(
                        missionRecord
                                .status
                                .eq(MissionRecordStatus.IN_PROGRESS)
                                .and(missionRecord.createdAt.before(currentTime)))
                .execute();
    }

    @Override
    public List<MissionTabResponse> findAllTabMissionsByMemberAndStatus(
            Member member, MissionRecordStatus status) {
        DateTemplate<String> completedAt =
                Expressions.dateTemplate(
                        String.class,
                        "DATE_FORMAT({0}, {1})",
                        missionRecord.updatedAt,
                        ConstantImpl.create("%Y-%m-%d"));
        return queryFactory
                .select(
                        Projections.constructor(
                                MissionTabResponse.class,
                                missionRecord.id.as("recordId"),
                                missionRecord.imageUrl,
                                missionRecord.status,
                                mission.title.as("missionTitle"),
                                mission.illustrationUrl,
                                missionRecord.content,
                                completedAt))
                .from(missionRecord)
                .leftJoin(missionRecord.missionHistory, missionHistory)
                .on(missionHistory.id.eq(missionRecord.missionHistory.id))
                .leftJoin(missionHistory.mission, mission)
                .on(mission.id.eq(missionHistory.mission.id))
                .where(missionRecord.member.eq(member).and(missionRecord.status.eq(status)))
                .orderBy(missionRecord.updatedAt.desc())
                .fetch();
    }

    private BooleanExpression isMemberId(Long memberId) {
        return missionRecord.member.id.eq(memberId);
    }

    private BooleanExpression createdAtFrom(LocalDateTime createdAt) {
        return missionRecord.createdAt.goe(createdAt);
    }

    private BooleanExpression isCompleted() {
        return missionRecord.status.eq(MissionRecordStatus.COMPLETED);
    }

    private BooleanExpression inDisplays(List<MissionRecordDisplay> displays) {
        return missionRecord.display.in(displays);
    }
}
