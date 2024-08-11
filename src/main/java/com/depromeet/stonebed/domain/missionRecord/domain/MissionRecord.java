package com.depromeet.stonebed.domain.missionRecord.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
public class MissionRecord extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "record_id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "mission_history_id", nullable = false)
    private MissionHistory missionHistory;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Schema(description = "미션 이미지 URL", example = "./missionRecord.jpg")
    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MissionRecordStatus status;

    @Schema(description = "미션 기록 텍스트", example = "미션 완료 소감")
    @Column(name = "text", nullable = true)
    private String text;

    @Builder
    public MissionRecord(
            Member member,
            MissionHistory missionHistory,
            String imageUrl,
            MissionRecordStatus status,
            String text) {
        this.member = member;
        this.missionHistory = missionHistory;
        this.imageUrl = imageUrl;
        this.status = status;
        this.text = text;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateText(String text) {
        this.text = text;
    }

    public void updateStatus(MissionRecordStatus status) {
        this.status = status;
    }
}
