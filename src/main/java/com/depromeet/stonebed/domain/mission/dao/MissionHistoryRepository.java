package com.depromeet.stonebed.domain.mission.dao;

import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionHistoryRepository
        extends JpaRepository<MissionHistory, Long>, MissionHistoryRepositoryCustom {
    Optional<MissionHistory> findByAssignedDate(LocalDate date);

    List<MissionHistory> findByAssignedDateBefore(LocalDate date);
}
