package com.depromeet.stonebed.domain.missionRecord.domain;

import com.depromeet.stonebed.domain.comment.domain.Comment;
import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionHistory.domain.MissionHistory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "mission_record")
public class MissionRecord extends BaseTimeEntity {

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

    @OneToMany(mappedBy = "missionRecord", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @Schema(description = "미션 이미지 URL", example = "./missionRecord.jpg")
    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private MissionRecordStatus status;

    @Schema(description = "미션 기록 컨텐츠", example = "미션 완료 소감")
    @Column(name = "content")
    private String content;

    @Enumerated(EnumType.STRING)
    @Schema(description = "미션 기록 ", example = "PUBLIC")
    @Column(name = "display", nullable = false)
    @ColumnDefault("'PUBLIC'")
    private MissionRecordDisplay display;

    @Builder(access = AccessLevel.PRIVATE)
    public MissionRecord(
            Member member,
            MissionHistory missionHistory,
            String imageUrl,
            MissionRecordStatus status,
            String content,
            MissionRecordDisplay display) {
        this.member = member;
        this.missionHistory = missionHistory;
        this.imageUrl = imageUrl;
        this.status = status;
        this.content = content;
        this.display = display;
    }

    public static MissionRecord createMissionRecord(Member member, MissionHistory missionHistory) {
        return MissionRecord.builder()
                .member(member)
                .missionHistory(missionHistory)
                .status(MissionRecordStatus.IN_PROGRESS)
                .display(MissionRecordDisplay.PUBLIC)
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

    public void updateDisplay(MissionRecordDisplay display) {
        this.display = display;
    }
}
