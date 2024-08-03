package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface MissionRecordRepositoryCustom {
    List<MissionRecord> findByMemberIdWithPagination(Long memberId, Pageable pageable);

    List<MissionRecord> findByMemberIdAndCreatedAtFromWithPagination(
            Long memberId, LocalDateTime createdAt, Pageable pageable);

    List<MissionRecord> findAllByStatus(MissionRecordStatus status);
}
