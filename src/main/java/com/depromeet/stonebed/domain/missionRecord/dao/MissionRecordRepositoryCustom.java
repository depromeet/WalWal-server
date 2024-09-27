package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordDisplay;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
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

    List<MissionTabResponse> findAllTabMissionsByMemberAndStatus(
            Member member, MissionRecordStatus status);
}
