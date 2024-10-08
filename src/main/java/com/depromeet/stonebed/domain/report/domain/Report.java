package com.depromeet.stonebed.domain.report.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
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
public class Report extends BaseTimeEntity {

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

    @Builder(access = AccessLevel.PRIVATE)
    private Report(MissionRecord missionRecord, Member member, String reason, String details) {
        this.missionRecord = missionRecord;
        this.member = member;
        this.reason = reason;
        this.details = details;
    }

    public static Report createReport(
            MissionRecord missionRecord, Member member, String reportReason, String details) {
        return Report.builder()
                .missionRecord(missionRecord)
                .member(member)
                .reason(reportReason)
                .details(details)
                .build();
    }
}
