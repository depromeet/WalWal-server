package com.depromeet.stonebed.domain.mission.dao;

import static com.depromeet.stonebed.domain.mission.domain.QMissionHistory.missionHistory;

import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionHistoryRepositoryImpl implements MissionHistoryRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<MissionHistory> findLatestOneByMissionId(Long missionId) {
        return Optional.ofNullable(
                queryFactory
                        .selectFrom(missionHistory)
                        .where(missionHistory.mission.id.eq(missionId))
                        .orderBy(missionHistory.assignedDate.desc())
                        .fetchFirst());
    }
}
