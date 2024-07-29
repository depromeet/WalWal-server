package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository
        extends JpaRepository<MissionRecord, Long>, MissionRecordRepositoryCustom {
    Long countByMemberIdAndStatus(Long memberId, MissionRecordStatus status);
}
