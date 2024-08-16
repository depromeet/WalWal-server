package com.depromeet.stonebed.domain.mission.dao.mission;

import static com.depromeet.stonebed.domain.mission.domain.QMission.mission;
import static com.depromeet.stonebed.domain.mission.domain.QMissionHistory.missionHistory;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MissionRepositoryImpl implements MissionRepositoryCustom {
    private final JPAQueryFactory queryFactory;

    @Override
    public List<Mission> findNotInMissions(List<Mission> missions) {
        return queryFactory.selectFrom(mission).where(mission.notIn(missions)).fetch();
    }

    @Override
    public List<Mission> findMissionsAssignedBefore(LocalDate assignedDate) {
        return queryFactory
                .select(missionHistory.mission)
                .from(missionHistory)
                .where(missionHistory.assignedDate.before(assignedDate))
                .fetch();
    }
}
