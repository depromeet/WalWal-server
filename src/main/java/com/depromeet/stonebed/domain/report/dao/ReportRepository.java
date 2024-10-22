package com.depromeet.stonebed.domain.report.dao;

import com.depromeet.stonebed.domain.report.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReportRepository extends JpaRepository<Report, Long> {}
