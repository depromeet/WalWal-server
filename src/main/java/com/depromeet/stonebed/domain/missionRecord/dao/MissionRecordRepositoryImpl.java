package com.depromeet.stonebed.domain.missionRecord.dao;

import static com.depromeet.stonebed.domain.missionRecord.domain.QMissionRecord.missionRecord;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRecordRepositoryImpl implements MissionRecordRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<MissionRecord> findByMemberId(Long memberId, Pageable pageable) {
        return queryFactory
                .selectFrom(missionRecord)
                .where(missionRecord.member.id.eq(memberId))
                .orderBy(missionRecord.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public List<MissionRecord> findByMemberIdAndCreatedAtAfter(
            Long memberId, LocalDateTime createdAt, Pageable pageable) {
        return queryFactory
                .selectFrom(missionRecord)
                .where(
                        missionRecord
                                .member
                                .id
                                .eq(memberId)
                                .and(missionRecord.createdAt.gt(createdAt)))
                .orderBy(missionRecord.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    @Override
    public Optional<MissionRecord> findFirstByMemberIdAndCreatedAt(
            Long memberId, LocalDate createdAt) {
        MissionRecord record =
                queryFactory
                        .selectFrom(missionRecord)
                        .where(
                                missionRecord
                                        .member
                                        .id
                                        .eq(memberId)
                                        .and(missionRecord.createdAt.year().eq(createdAt.getYear()))
                                        .and(
                                                missionRecord
                                                        .createdAt
                                                        .month()
                                                        .eq(createdAt.getMonthValue()))
                                        .and(
                                                missionRecord
                                                        .createdAt
                                                        .dayOfMonth()
                                                        .eq(createdAt.getDayOfMonth())))
                        .orderBy(missionRecord.createdAt.asc())
                        .fetchFirst();
        return Optional.ofNullable(record);
    }
}
