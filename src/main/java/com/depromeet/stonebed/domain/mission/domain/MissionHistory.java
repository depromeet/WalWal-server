package com.depromeet.stonebed.domain.mission.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.RaisePet;
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
        uniqueConstraints = {@UniqueConstraint(columnNames = {"assigned_date", "raise_pet"})})
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

    @Enumerated(EnumType.STRING)
    @Column(name = "raise_pet", nullable = false)
    private RaisePet raisePet;

    @Builder(access = AccessLevel.PRIVATE)
    public MissionHistory(Mission mission, LocalDate assignedDate, RaisePet raisePet) {
        this.mission = mission;
        this.assignedDate = assignedDate;
        this.raisePet = raisePet;
    }

    public static MissionHistory createMissionHistory(
            Mission mission, LocalDate assignedDate, RaisePet raisePet) {
        return MissionHistory.builder()
                .mission(mission)
                .assignedDate(assignedDate)
                .raisePet(raisePet)
                .build();
    }
}
