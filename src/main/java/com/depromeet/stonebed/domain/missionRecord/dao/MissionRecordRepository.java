package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionHistory.domain.MissionHistory;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MissionRecordRepository
        extends JpaRepository<MissionRecord, Long>, MissionRecordRepositoryCustom {
    Optional<MissionRecord> findByMemberAndMissionHistory(
            Member member, MissionHistory missionHistory);

    Long countByMemberIdAndStatus(Long memberId, MissionRecordStatus status);

    List<MissionRecord> findAllByCreatedAtBetweenAndStatusNot(
            LocalDateTime startTime, LocalDateTime endTime, MissionRecordStatus status);

    List<MissionRecord> findByIdIn(List<Long> ids);

    boolean existsByMemberAndMissionHistoryAndStatusAndCreatedAtBetween(
            Member member,
            MissionHistory missionHistory,
            MissionRecordStatus status,
            LocalDateTime startDateTime,
            LocalDateTime endDateTime);

    // Delete
    @Modifying
    @Query("DELETE FROM MissionRecord mr WHERE mr.member.id = :memberId")
    void deleteAllByMember(@Param("memberId") Long memberId);

    List<MissionRecord> findAllByMember(Member member);
}
