package com.depromeet.stonebed.domain.member.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.member.dto.request.MemberProfileUpdateRequest;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.stonebed.domain.member.dto.response.MemberInfoResponse;
import com.depromeet.stonebed.global.util.MemberUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest extends FixtureMonkeySetUp {

    @InjectMocks private MemberService memberService;

    @Mock private MemberUtil memberUtil;

    @Test
    void 사용자_정보를_조회한다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(memberUtil.getCurrentMember()).thenReturn(member);

        // when
        MemberInfoResponse result = memberService.findMemberInfo();

        // then
        assertNotNull(result);
        assertEquals(member.getId(), result.memberId());
        assertEquals(member.getProfile().getNickname(), result.nickname());
        assertEquals(member.getProfile().getProfileImageUrl(), result.profileImageUrl());
        assertEquals(member.getRaisePet(), result.raisePet());
        verify(memberUtil).getCurrentMember();
    }

    @Test
    void 닉네임을_검증한다() {
        // given
        NicknameCheckRequest request = fixtureMonkey.giveMeOne(NicknameCheckRequest.class);

        // when
        memberService.checkNickname(request);

        // then
        verify(memberUtil).checkNickname(request);
    }

    @Test
    void 사용자_프로필을_수정한다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(memberUtil.getCurrentMember()).thenReturn(member);

        String nickname = fixtureMonkey.giveMeOne(String.class);
        String profileImageUrl = fixtureMonkey.giveMeOne(String.class);
        MemberProfileUpdateRequest request =
                new MemberProfileUpdateRequest(nickname, profileImageUrl);

        // when
        memberService.modifyMemberProfile(request);

        // then
        verify(memberUtil).getCurrentMember();

        Profile updatedProfile = member.getProfile();
        assertEquals(nickname, updatedProfile.getNickname());
        assertEquals(profileImageUrl, updatedProfile.getProfileImageUrl());
    }
}
