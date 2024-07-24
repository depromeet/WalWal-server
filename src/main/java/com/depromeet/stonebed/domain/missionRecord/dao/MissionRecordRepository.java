package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {
    List<MissionRecord> findByMemberId(Long memberId, Pageable pageable);

    List<MissionRecord> findByMemberIdAndCreatedAtAfter(
            Long memberId, LocalDateTime createdAt, Pageable pageable);

    Optional<MissionRecord> findFirstByMemberIdAndCreatedAt(Long memberId, LocalDate createdAt);
}
