package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {
    List<MissionRecord> findByMemberId(Long memberId);

    Optional<MissionRecord> findFirstByMemberIdAndCreatedAt(Long memberId, LocalDate createdAt);
}
