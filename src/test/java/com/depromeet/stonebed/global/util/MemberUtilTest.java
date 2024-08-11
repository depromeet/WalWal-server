package com.depromeet.stonebed.global.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MemberUtilTest {

    @InjectMocks private MemberUtil memberUtil;

    @Mock private SecurityUtil securityUtil;

    @Mock private MemberRepository memberRepository;

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
    void getCurrentMember_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(securityUtil.getCurrentMemberId()).thenReturn(member.getId());
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        // when
        Member result = memberUtil.getCurrentMember();

        // then
        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
    }

    @Test
    void getCurrentMember_실패_MEMBER_NOT_FOUND() {
        // given
        Long memberId = fixtureMonkey.giveMeOne(Long.class);
        when(securityUtil.getCurrentMemberId()).thenReturn(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> memberUtil.getCurrentMember());
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getMemberByMemberId_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(memberRepository.findById(member.getId())).thenReturn(Optional.of(member));

        // when
        Member result = memberUtil.getMemberByMemberId(member.getId());

        // then
        assertNotNull(result);
        assertEquals(member.getId(), result.getId());
    }

    @Test
    void getMemberByMemberId_실패_MEMBER_NOT_FOUND() {
        // given
        Long memberId = fixtureMonkey.giveMeOne(Long.class);
        when(memberRepository.findById(memberId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> memberUtil.getMemberByMemberId(memberId));
        assertEquals(ErrorCode.MEMBER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getMemberRole_성공() {
        // given
        String role = fixtureMonkey.giveMeOne(String.class);
        when(securityUtil.getCurrentMemberRole()).thenReturn(role);

        // when
        String result = memberUtil.getMemberRole();

        // then
        assertNotNull(result);
        assertEquals(role, result);
    }

    @Test
    void checkNickname_성공() {
        // given
        String nickname = "validNickname";
        NicknameCheckRequest request = new NicknameCheckRequest(nickname);
        when(memberRepository.existsByProfileNickname(nickname)).thenReturn(false);

        // when & then
        assertDoesNotThrow(() -> memberUtil.checkNickname(request));
    }

    @Test
    void checkNickname_실패_MEMBER_ALREADY_NICKNAME() {
        // given
        String nickname = "duplicateNickname";
        NicknameCheckRequest request = new NicknameCheckRequest(nickname);
        when(memberRepository.existsByProfileNickname(nickname)).thenReturn(true);

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> memberUtil.checkNickname(request));
        assertEquals(ErrorCode.MEMBER_ALREADY_NICKNAME, exception.getErrorCode());
    }

    @Test
    void checkNickname_실패_MEMBER_INVALID_NICKNAME_길이_1() {
        // given
        String nickname = "a";
        NicknameCheckRequest request = new NicknameCheckRequest(nickname);
        when(memberRepository.existsByProfileNickname(nickname)).thenReturn(false);

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> memberUtil.checkNickname(request));
        assertEquals(ErrorCode.MEMBER_INVALID_NICKNAME, exception.getErrorCode());
    }

    @Test
    void checkNickname_실패_MEMBER_INVALID_NICKNAME_길이_15() {
        // given
        String nickname = "a".repeat(15);
        NicknameCheckRequest request = new NicknameCheckRequest(nickname);
        when(memberRepository.existsByProfileNickname(nickname)).thenReturn(false);

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> memberUtil.checkNickname(request));
        assertEquals(ErrorCode.MEMBER_INVALID_NICKNAME, exception.getErrorCode());
    }
}
