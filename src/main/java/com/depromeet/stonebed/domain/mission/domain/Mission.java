package com.depromeet.stonebed.domain.mission.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.RaisePet;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Mission extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mission_id")
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "illustration_url")
    private String illustrationUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "raise_pet", nullable = false)
    private RaisePet raisePet;

    @NotBlank
    @Size(max = 100)
    @Column(name = "complete_message")
    private String completeMessage;

    @Builder
    public Mission(String title, RaisePet raisePet, String completeMessage) {
        this.title = title;
        this.raisePet = raisePet;
        this.completeMessage = completeMessage;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateIllustrationUrl(String illustrationUrl) {
        this.illustrationUrl = illustrationUrl;
    }

    public void updateRaisePet(RaisePet raisePet) {
        this.raisePet = raisePet;
    }

    public void updateCompleteMessage(String completeMessage) {
        this.completeMessage = completeMessage;
    }
}
