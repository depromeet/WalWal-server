package com.depromeet.stonebed.domain.mission.dao.mission;

import static com.depromeet.stonebed.domain.mission.domain.QMission.mission;
import static com.depromeet.stonebed.domain.mission.domain.QMissionHistory.missionHistory;

import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.querydsl.core.types.dsl.BooleanExpression;
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
    public List<Mission> findNotInMissionsAndByRaisePet(List<Mission> missions, RaisePet raisePet) {
        return queryFactory
                .selectFrom(mission)
                .where(mission.notIn(missions).and(mission.raisePet.eq(raisePet)))
                .fetch();
    }

    @Override
    public List<Mission> findMissionsAssignedAfterAndByRaisePet(
            LocalDate assignedDate, RaisePet raisePet) {
        return queryFactory
                .select(missionHistory.mission)
                .from(missionHistory)
                .join(missionHistory.mission, mission)
                .where(assignedDateAfter(assignedDate).and(mission.raisePet.eq(raisePet)))
                .fetch();
    }

    private BooleanExpression assignedDateAfter(LocalDate assignedDate) {
        return missionHistory.assignedDate.after(assignedDate);
    }
}
