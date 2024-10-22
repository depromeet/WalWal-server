package com.depromeet.stonebed.domain.report.dao;

import com.depromeet.stonebed.domain.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface ReportRepository extends JpaRepository<Report, Long> {
    @Modifying
    @Query("DELETE FROM Report r WHERE r.member.id = :memberId")
    void deleteAllByMember(Long memberId);
}
