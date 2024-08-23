package com.depromeet.stonebed.domain.mission.dao.missionHistory;

import static com.depromeet.stonebed.domain.mission.domain.QMissionHistory.missionHistory;

import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionHistoryRepositoryImpl implements MissionHistoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MissionHistory> findLatestOneByMissionIdRaisePet(
            Long missionId, RaisePet raisePet) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(missionHistory)
                        .where(
                                missionHistory
                                        .mission
                                        .id
                                        .eq(missionId)
                                        .and(missionHistory.raisePet.eq(raisePet)))
                        .orderBy(missionHistory.assignedDate.desc())
                        .fetchFirst());
    }

    @Override
    public Optional<MissionHistory> findByAssignedDateAndRaisePet(
            LocalDate date, RaisePet raisePet) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(missionHistory)
                        .where(
                                missionHistory
                                        .assignedDate
                                        .eq(date)
                                        .and(missionHistory.mission.raisePet.eq(raisePet)))
                        .fetchFirst());
    }
}
