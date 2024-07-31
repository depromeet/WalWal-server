package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository
        extends JpaRepository<MissionRecord, Long>, MissionRecordRepositoryCustom {
    Optional<MissionRecord> findByMemberAndMission(Member member, Mission mission);

    Long countByMemberIdAndStatus(Long memberId, MissionRecordStatus status);
}
