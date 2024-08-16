package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository
        extends JpaRepository<MissionRecord, Long>, MissionRecordRepositoryCustom {
    Optional<MissionRecord> findByMemberAndMissionHistory(
            Member member, MissionHistory missionHistory);

    Long countByMemberIdAndStatus(Long memberId, MissionRecordStatus status);

    List<MissionRecord> findAllByStatus(MissionRecordStatus status);
}
