package com.depromeet.stonebed.domain.missionRecord.dao;

import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecord.missionRecord;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    public List<MissionRecord> findByMemberIdWithPagination(Long memberId, Pageable pageable) {
        return queryFactory
                .selectFrom(missionRecord)
                .where(isMemberId(memberId))
                .orderBy(missionRecord.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<MissionRecord> findByMemberIdAndCreatedAtFromWithPagination(
            Long memberId, LocalDateTime createdAt, Pageable pageable) {
        return queryFactory
                .selectFrom(missionRecord)
                .where(missionRecord.member.id.eq(memberId).and(createdAtFrom(createdAt)))
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

    private BooleanExpression isMemberId(Long memberId) {
        return missionRecord.member.id.eq(memberId);
    }

    private BooleanExpression createdAtFrom(LocalDateTime createdAt) {
        return missionRecord.createdAt.goe(createdAt);
    }
}
