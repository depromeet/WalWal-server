package com.depromeet.stonebed.domain.mission.dao;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import java.time.LocalDate;
import java.util.List;

public interface MissionRepositoryCustom {
    List<Mission> findNotInMissions(List<Mission> missions);

    List<Mission> findMissionsAssignedBefore(LocalDate assignedDate);
}
