package com.depromeet.stonebed.domain.mission.dao.missionHistory;

import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import java.time.LocalDate;
import java.util.Optional;

public interface MissionHistoryRepositoryCustom {
    Optional<MissionHistory> findLatestOneByMissionId(Long missionId);

    Optional<MissionHistory> findByAssignedDateAndRaisePet(LocalDate date, RaisePet raisePet);
}
