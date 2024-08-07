package com.depromeet.stonebed.domain.member.domain;

import static org.junit.jupiter.api.Assertions.*;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class MemberTest {

    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setUp() {
        fixtureMonkey =
                FixtureMonkey.builder()
                        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build();
    }

    @Test
    void createMember_성공() {
        // given
        Profile profile = fixtureMonkey.giveMeOne(Profile.class);
        OauthInfo oauthInfo = fixtureMonkey.giveMeOne(OauthInfo.class);
        MemberStatus status = MemberStatus.NORMAL;
        MemberRole role = MemberRole.USER;
        RaisePet raisePet = fixtureMonkey.giveMeOne(RaisePet.class);

        // when
        Member member = Member.createMember(profile, oauthInfo, status, role, raisePet);

        // then
        assertNotNull(member);
        assertEquals(profile, member.getProfile());
        assertEquals(oauthInfo, member.getOauthInfo());
        assertEquals(status, member.getStatus());
        assertEquals(role, member.getRole());
        assertEquals(raisePet, member.getRaisePet());
    }

    @Test
    void createOAuthMember_성공() {
        // given
        OAuthProvider provider = OAuthProvider.KAKAO;
        String oauthId = fixtureMonkey.giveMeOne(String.class);
        String email = fixtureMonkey.giveMeOne(String.class);

        // when
        Member member = Member.createOAuthMember(provider, oauthId, email);

        // then
        assertNotNull(member);
        assertEquals(provider.getValue(), member.getOauthInfo().getOauthProvider());
        assertEquals(oauthId, member.getOauthInfo().getOauthId());
        assertEquals(email, member.getOauthInfo().getOauthEmail());
        assertEquals(MemberRole.TEMPORARY, member.getRole());
    }

    @Test
    void updateLastLoginAt_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);

        // when
        member.updateLastLoginAt();

        // then
        assertNotNull(member.getLastLoginAt());
        assertTrue(member.getLastLoginAt().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void updateRaisePet_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        RaisePet newRaisePet = fixtureMonkey.giveMeOne(RaisePet.class);

        // when
        member.updateRaisePet(newRaisePet);

        // then
        assertEquals(newRaisePet, member.getRaisePet());
    }

    @Test
    void updateProfile_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        Profile newProfile = fixtureMonkey.giveMeOne(Profile.class);

        // when
        member.updateProfile(newProfile);

        // then
        assertEquals(newProfile, member.getProfile());
    }

    @Test
    void updateMemberRole_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MemberRole newRole = MemberRole.ADMIN;

        // when
        member.updateMemberRole(newRole);

        // then
        assertEquals(newRole, member.getRole());
    }

    @Test
    void withdrawal_성공() {
        // given
        Member member =
                fixtureMonkey
                        .giveMeBuilder(Member.class)
                        .set("role", MemberRole.USER)
                        .set("status", MemberStatus.NORMAL)
                        .sample();
        member.updateMemberRole(MemberRole.USER);

        // when
        member.withdrawal();

        // then
        assertEquals(MemberStatus.DELETED, member.getStatus());
    }

    @Test
    void withdrawal_실패_MEMBER_ALREADY_DELETED() {
        // given
        Member member =
                fixtureMonkey
                        .giveMeBuilder(Member.class)
                        .set("role", MemberRole.USER)
                        .set("status", MemberStatus.DELETED)
                        .sample();

        // when & then
        CustomException exception = assertThrows(CustomException.class, member::withdrawal);
        assertEquals(ErrorCode.MEMBER_ALREADY_DELETED, exception.getErrorCode());
    }
}
