package com.depromeet.stonebed.domain.member.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @InjectMocks private MemberService memberService;

    @Mock private MemberUtil memberUtil;

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
    void 사용자_정보를_조회한다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        when(memberUtil.getCurrentMember()).thenReturn(member);

        // when
        Member result = memberService.findMemberInfo();

        // then
        assertNotNull(result);
        assertEquals(member, result);
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
}
