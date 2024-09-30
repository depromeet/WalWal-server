package com.depromeet.stonebed.domain.report.domain;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import jakarta.persistence.Entity;
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
@Table(name = "feed_report")
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_record_id", nullable = false)
    private MissionRecord missionRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private String reason;

    private String details;

    @Builder
    public Report(
            MissionRecord missionRecord, Member member, ReportReason reportReason, String details) {
        this.missionRecord = missionRecord;
        this.member = member;
        this.reason = reportReason.getValue();
        this.details = details;
    }
}
