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
public class MissionHistory extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_history_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "mission_id", nullable = false)
    private Mission mission;

    @Column(name = "assigned_date", nullable = false, unique = true)
    private LocalDate assignedDate;

    @Builder
    public MissionHistory(Mission mission, LocalDate assignedDate) {
        this.mission = mission;
        this.assignedDate = assignedDate;
    }
}
