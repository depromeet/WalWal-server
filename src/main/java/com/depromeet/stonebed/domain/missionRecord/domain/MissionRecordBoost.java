package com.depromeet.stonebed.domain.missionRecord.domain;

import com.depromeet.stonebed.domain.common.BaseFullTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@Table(name = "mission_record_boost")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE mission_record_boost SET deleted_at = NOW() WHERE boost_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MissionRecordBoost extends BaseFullTimeEntity {
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
