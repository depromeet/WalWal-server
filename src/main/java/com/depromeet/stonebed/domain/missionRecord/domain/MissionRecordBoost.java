package com.depromeet.stonebed.domain.missionRecord.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "mission_record_boost")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionRecordBoost extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "boost_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_record_id", nullable = false)
    private MissionRecord missionRecord;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "count", nullable = false)
    private Long count;

    @Builder
    public MissionRecordBoost(MissionRecord missionRecord, Member member, Long count) {
        this.missionRecord = missionRecord;
        this.member = member;
        this.count = count;
    }
}
