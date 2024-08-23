package com.depromeet.stonebed.domain.missionRecord.domain;

import com.depromeet.stonebed.domain.common.BaseFullTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "mission_record",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_member_mission_history",
                    columnNames = {"member_id", "mission_history_id"})
        })
@SQLDelete(sql = "UPDATE mission_record SET deleted_at = NOW() WHERE record_id = ?")
@SQLRestriction("deleted_at IS NULL")
public class MissionRecord extends BaseFullTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mission_history_id", nullable = false)
    private MissionHistory missionHistory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Schema(description = "미션 이미지 URL", example = "./missionRecord.jpg")
    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MissionRecordStatus status;

    @Schema(description = "미션 기록 컨텐츠", example = "미션 완료 소감")
    @Column(name = "content")
    private String content;

    @Builder
    public MissionRecord(
            Member member,
            MissionHistory missionHistory,
            String imageUrl,
            MissionRecordStatus status,
            String content) {
        this.member = member;
        this.missionHistory = missionHistory;
        this.imageUrl = imageUrl;
        this.status = status;
        this.content = content;
    }

    public static MissionRecord createMissionRecord(
            String content, Member member, MissionHistory missionHistory) {
        return MissionRecord.builder()
                .content(content)
                .member(member)
                .missionHistory(missionHistory)
                .status(MissionRecordStatus.IN_PROGRESS)
                .build();
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateContent(String content) {
        this.content = content;
    }

    public void updateStatus(MissionRecordStatus status) {
        this.status = status;
    }
}
