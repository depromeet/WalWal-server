package com.depromeet.stonebed.domain.mission.dao;

import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import java.time.LocalDate;
import java.util.List;

public interface MissionRepositoryCustom {
    List<Mission> findMissionsAssignedAfterAndByRaisePet(LocalDate assignedDate, RaisePet raisePet);

    List<Mission> findNotInMissionsAndByRaisePet(List<Mission> missions, RaisePet raisePet);
}
