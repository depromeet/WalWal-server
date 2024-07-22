package com.depromeet.stonebed.domain.mission.dao;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    @Query("SELECT m FROM Mission m WHERE m.id NOT IN :mission_ids")
    List<Mission> findAllByIdNotIn(@Param("mission_ids") List<Long> mission_ids);
}
