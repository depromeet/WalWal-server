package com.depromeet.stonebed.domain.fcm.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import com.depromeet.stonebed.domain.member.domain.Member;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "fcm_notification")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FcmNotification extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FcmNotificationType type;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String message;

    @Column private Long targetId;

    @Column(nullable = false)
    private Boolean isRead = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    private FcmNotification(
            FcmNotificationType type,
            String title,
            String message,
            Member member,
            Long targetId,
            Boolean isRead) {
        this.type = type;
        this.title = title;
        this.message = message;
        this.member = member;
        this.targetId = targetId;
        this.isRead = isRead;
    }

    public static FcmNotification create(
            FcmNotificationType type,
            String title,
            String message,
            Member member,
            Long targetId,
            Boolean isRead) {
        return new FcmNotification(type, title, message, member, targetId, isRead);
    }

    public void markAsRead() {
        this.isRead = true;
    }
}
