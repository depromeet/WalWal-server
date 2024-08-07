package com.depromeet.stonebed.domain.auth.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.response.AuthTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @InjectMocks private AuthService authService;

    @Mock private JwtTokenService jwtTokenService;
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
    void 소셜로그인_KAKAO_성공() {
        // given
        OAuthProvider provider = OAuthProvider.KAKAO;
        String oauthId = fixtureMonkey.giveMeOne(String.class);
        String email = fixtureMonkey.giveMeOne(String.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        member.updateMemberRole(MemberRole.USER);

        when(memberRepository.findByOauthInfoOauthProviderAndOauthInfoOauthId(
                        provider.getValue(), oauthId))
                .thenReturn(Optional.of(member));
        when(jwtTokenService.generateTokenPair(member.getId(), MemberRole.USER))
                .thenReturn(new TokenPairResponse("accessToken", "refreshToken"));

        // when
        AuthTokenResponse response = authService.socialLogin(provider, oauthId, email);

        // then
        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    void 소셜로그인_APPLE_성공() {
        // given
        OAuthProvider provider = OAuthProvider.APPLE;
        String oauthId = fixtureMonkey.giveMeOne(String.class);
        String email = fixtureMonkey.giveMeOne(String.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        member.updateMemberRole(MemberRole.USER);

        when(memberRepository.findByOauthInfoOauthProviderAndOauthInfoOauthId(
                        provider.getValue(), oauthId))
                .thenReturn(Optional.of(member));
        when(jwtTokenService.generateTokenPair(member.getId(), MemberRole.USER))
                .thenReturn(new TokenPairResponse("accessToken", "refreshToken"));

        // when
        AuthTokenResponse response = authService.socialLogin(provider, oauthId, email);

        // then
        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
    }

    @Test
    void 소셜로그인_임시_회원가입_성공() {
        // given
        OAuthProvider provider = OAuthProvider.KAKAO;
        String oauthId = fixtureMonkey.giveMeOne(String.class);
        String email = fixtureMonkey.giveMeOne(String.class);
        Member newMember = Member.createOAuthMember(provider, oauthId, email);
        TokenPairResponse temporaryTokenPair = new TokenPairResponse("accessToken", "refreshToken");

        when(memberRepository.findByOauthInfoOauthProviderAndOauthInfoOauthId(
                        provider.getValue(), oauthId))
                .thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(newMember);
        when(jwtTokenService.generateTemporaryTokenPair(any(Member.class)))
                .thenReturn(temporaryTokenPair);

        // when
        AuthTokenResponse response = authService.socialLogin(provider, oauthId, email);

        // then
        assertNotNull(response);
        assertEquals("accessToken", response.accessToken());
        assertEquals("refreshToken", response.refreshToken());
        assertTrue(response.isTemporaryToken());
        verify(memberRepository).save(any(Member.class));
        verify(jwtTokenService).generateTemporaryTokenPair(any(Member.class));
    }
}
