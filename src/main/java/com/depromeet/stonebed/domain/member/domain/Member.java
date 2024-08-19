package com.depromeet.stonebed.domain.member.domain;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE member SET status = 'DELETED' WHERE member_id = ?")
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    @Embedded private Profile profile = Profile.createProfile("", "");

    @Embedded private OauthInfo oauthInfo;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    @Enumerated(EnumType.STRING)
    private RaisePet raisePet;

    private LocalDateTime lastLoginAt;

    @Builder(access = AccessLevel.PRIVATE)
    public Member(
            Profile profile,
            OauthInfo oauthInfo,
            MemberStatus status,
            MemberRole role,
            RaisePet raisePet) {
        this.profile = profile;
        this.oauthInfo = oauthInfo;
        this.status = status;
        this.role = role;
        this.raisePet = raisePet;
    }

    public static Member createMember(
            Profile profile,
            OauthInfo oauthInfo,
            MemberStatus status,
            MemberRole role,
            RaisePet raisePet) {
        return Member.builder()
                .profile(profile)
                .oauthInfo(oauthInfo)
                .status(status)
                .role(role)
                .raisePet(raisePet)
                .build();
    }

    public static Member createOAuthMember(
            OAuthProvider oAuthProvider, String oauthId, String email) {
        OauthInfo oauthInfo = OauthInfo.createOauthInfo(oauthId, oAuthProvider.getValue(), email);

        return Member.createMember(
                Profile.createProfile("", ""),
                oauthInfo,
                MemberStatus.NORMAL,
                MemberRole.TEMPORARY,
                null);
    }

    public void updateLastLoginAt() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void updateRaisePet(RaisePet raisePet) {
        this.raisePet = raisePet;
    }

    public void updateProfile(Profile profile) {
        this.profile = profile;
    }

    public void updateMemberRole(MemberRole memberRole) {
        this.role = memberRole;
    }

    public void updateStatus(MemberStatus status) {
        this.status = status;
    }
}
