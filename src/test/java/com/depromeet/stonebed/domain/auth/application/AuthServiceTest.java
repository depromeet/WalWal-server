package com.depromeet.stonebed.domain.auth.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.dto.response.AuthTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
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
class AuthServiceTest extends FixtureMonkeySetUp {

    @InjectMocks private AuthService authService;

    @Mock private JwtTokenService jwtTokenService;

    @Mock private MemberRepository memberRepository;

    private String oauthId;
    private String email;
    private Member member;

    @BeforeEach
    void setUp() {
        oauthId = fixtureMonkey.giveMeOne(String.class);
        email = fixtureMonkey.giveMeOne(String.class);
        member = fixtureMonkey.giveMeOne(Member.class);
        member.updateMemberRole(MemberRole.USER);
    }

    @Test
    void 소셜로그인_KAKAO_로그인을_시도합니다() {
        // given
        OAuthProvider provider = OAuthProvider.KAKAO;

        when(memberRepository.findByOauthInfoOauthProviderAndOauthInfoOauthId(
                        provider.getValue(), oauthId))
                .thenReturn(Optional.of(member));
        when(jwtTokenService.generateTokenPair(member.getId(), MemberRole.USER))
                .thenReturn(new TokenPairResponse("accessToken", "refreshToken"));

        // when
        AuthTokenResponse response = authService.socialLogin(provider, oauthId, email);

        // then
        assertCommonAssertions(response, "accessToken", "refreshToken");
    }

    @Test
    void 소셜로그인_APPLE_로그인을_시도합니다() {
        // given
        OAuthProvider provider = OAuthProvider.APPLE;

        when(memberRepository.findByOauthInfoOauthProviderAndOauthInfoOauthId(
                        provider.getValue(), oauthId))
                .thenReturn(Optional.of(member));
        when(jwtTokenService.generateTokenPair(member.getId(), MemberRole.USER))
                .thenReturn(new TokenPairResponse("accessToken", "refreshToken"));

        // when
        AuthTokenResponse response = authService.socialLogin(provider, oauthId, email);

        // then
        assertCommonAssertions(response, "accessToken", "refreshToken");
    }

    @Test
    void 소셜로그인_임시_회원가입을_시도합니다() {
        // given
        OAuthProvider provider = OAuthProvider.KAKAO;
        TokenPairResponse temporaryTokenPair = new TokenPairResponse("accessToken", "refreshToken");
        Member newMember = Member.createOAuthMember(provider, oauthId, email);

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

    private void assertCommonAssertions(
            AuthTokenResponse response, String expectedAccessToken, String expectedRefreshToken) {
        assertNotNull(response);
        assertEquals(expectedAccessToken, response.accessToken());
        assertEquals(expectedRefreshToken, response.refreshToken());
    }
}
