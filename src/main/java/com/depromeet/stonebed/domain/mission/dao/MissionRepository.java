package com.depromeet.stonebed.domain.mission.dao;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    List<Mission> findMissionsByIdNotIn(List<Long> mission_ids);
}
