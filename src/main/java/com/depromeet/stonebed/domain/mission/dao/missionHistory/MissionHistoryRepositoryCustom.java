package com.depromeet.stonebed.domain.mission.dao.missionHistory;

import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import java.util.Optional;

public interface MissionHistoryRepositoryCustom {
    Optional<MissionHistory> findLatestOneByMissionId(Long missionId);
}
