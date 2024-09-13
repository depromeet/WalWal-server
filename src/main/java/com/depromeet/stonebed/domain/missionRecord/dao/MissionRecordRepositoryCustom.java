package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordDisplay;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface MissionRecordRepositoryCustom {
    List<MissionRecord> findByMemberIdWithPagination(
            Long memberId, List<MissionRecordDisplay> displays, Pageable pageable);

    List<MissionRecord> findByMemberIdAndCreatedAtFromWithPagination(
            Long memberId,
            LocalDateTime createdAt,
            List<MissionRecordDisplay> displays,
            Pageable pageable);

    void updateExpiredMissionsToNotCompleted(LocalDateTime dateTime);
}
