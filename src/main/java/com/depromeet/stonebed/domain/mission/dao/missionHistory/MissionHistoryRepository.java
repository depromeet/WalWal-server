package com.depromeet.stonebed.domain.mission.dao.missionHistory;

import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionHistoryRepository
        extends JpaRepository<MissionHistory, Long>, MissionHistoryRepositoryCustom {
    Optional<MissionHistory> findByAssignedDate(LocalDate date);
}
