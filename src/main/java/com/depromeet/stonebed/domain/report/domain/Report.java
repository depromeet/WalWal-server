package com.depromeet.stonebed.domain.report.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "report")
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private Member reporter;

    @Enumerated(EnumType.STRING)
    @Column(name = "report_domain", nullable = false)
    private ReportDomain reportDomain;

    private Long targetId;

    private String reason;

    private String details;

    @Builder(access = AccessLevel.PRIVATE)
    private Report(
            Long targetId,
            Member reporter,
            ReportDomain reportDomain,
            String reason,
            String details) {
        this.targetId = targetId;
        this.reporter = reporter;
        this.reportDomain = reportDomain;
        this.reason = reason;
        this.details = details;
    }

    public static Report createReport(
            Long targetId,
            Member reporter,
            ReportDomain reportDomain,
            String reportReason,
            String details) {
        return Report.builder()
                .targetId(targetId)
                .reporter(reporter)
                .reportDomain(reportDomain)
                .reason(reportReason)
                .details(details)
                .build();
    }
}
