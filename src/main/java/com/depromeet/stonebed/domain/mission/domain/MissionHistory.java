package com.depromeet.stonebed.domain.mission.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "mission_history",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"assigned_date", "mission_id"})})
public class MissionHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "assigned_date", nullable = false)
    private LocalDate assignedDate;

    @Builder(access = AccessLevel.PRIVATE)
    public MissionHistory(Mission mission, LocalDate assignedDate) {
        this.mission = mission;
        this.assignedDate = assignedDate;
    }

    public static MissionHistory createMissionHistory(Mission mission, LocalDate assignedDate) {
        return MissionHistory.builder().mission(mission).assignedDate(assignedDate).build();
    }
}
