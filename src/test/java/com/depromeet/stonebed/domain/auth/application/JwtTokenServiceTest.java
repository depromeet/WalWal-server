package com.depromeet.stonebed.domain.auth.application;

import static com.depromeet.stonebed.global.common.constants.SecurityConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.stonebed.domain.auth.domain.RefreshToken;
import com.depromeet.stonebed.domain.auth.dto.AccessTokenDto;
import com.depromeet.stonebed.domain.auth.dto.RefreshTokenDto;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.global.util.JwtUtil;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class JwtTokenServiceTest {

    @Mock private JwtUtil jwtUtil;
    @Mock private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks private JwtTokenService jwtTokenService;

    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        fixtureMonkey =
                FixtureMonkey.builder()
                        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build();
    }

    @Test
    void generateTokenPair_성공() {
        // given
        Long memberId = fixtureMonkey.giveMeOne(Long.class);
        MemberRole memberRole = MemberRole.USER;
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        when(jwtUtil.generateAccessToken(memberId, memberRole)).thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(memberId)).thenReturn(refreshToken);

        // when
        TokenPairResponse tokenPair = jwtTokenService.generateTokenPair(memberId, memberRole);

        // then
        assertNotNull(tokenPair);
        assertEquals(accessToken, tokenPair.accessToken());
        assertEquals(refreshToken, tokenPair.refreshToken());
    }

    @Test
    void generateTemporaryTokenPair_성공() {
        // given
        Member temporaryMember = fixtureMonkey.giveMeOne(Member.class);
        String accessToken = "accessToken";
        String refreshToken = "refreshToken";

        when(jwtUtil.generateAccessToken(temporaryMember.getId(), MemberRole.TEMPORARY))
                .thenReturn(accessToken);
        when(jwtUtil.generateRefreshToken(temporaryMember.getId())).thenReturn(refreshToken);

        // when
        TokenPairResponse tokenPair = jwtTokenService.generateTemporaryTokenPair(temporaryMember);

        // then
        assertNotNull(tokenPair);
        assertEquals(accessToken, tokenPair.accessToken());
        assertEquals(refreshToken, tokenPair.refreshToken());
    }

    @Test
    void createAccessTokenDto_성공() {
        // given
        Long memberId = fixtureMonkey.giveMeOne(Long.class);
        MemberRole memberRole = MemberRole.USER;
        String tokenValue = "accessToken";
        when(jwtUtil.generateAccessTokenDto(memberId, memberRole))
                .thenReturn(new AccessTokenDto(memberId, memberRole, tokenValue));

        // when
        AccessTokenDto accessTokenDto = jwtTokenService.createAccessTokenDto(memberId, memberRole);

        // then
        assertNotNull(accessTokenDto);
        assertEquals(memberId, accessTokenDto.memberId());
        assertEquals(memberRole, accessTokenDto.memberRole());
        assertEquals(tokenValue, accessTokenDto.tokenValue());
    }

    @Test
    void retrieveAccessToken_성공() {
        // given
        Long memberId = fixtureMonkey.giveMeOne(Long.class);
        MemberRole memberRole = MemberRole.USER;
        String accessTokenValue = "accessToken";
        AccessTokenDto accessTokenDto = new AccessTokenDto(memberId, memberRole, accessTokenValue);

        when(jwtUtil.parseAccessToken(accessTokenValue)).thenReturn(accessTokenDto);

        // when
        AccessTokenDto result = jwtTokenService.retrieveAccessToken(accessTokenValue);

        // then
        assertNotNull(result);
        assertEquals(accessTokenDto, result);
    }

    @Test
    void retrieveRefreshToken_성공() {
        // given
        String refreshTokenValue = "refreshToken";
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(1L, refreshTokenValue, 3600L);
        RefreshToken refreshToken = new RefreshToken(1L, refreshTokenValue, 3600L);

        when(jwtUtil.parseRefreshToken(refreshTokenValue)).thenReturn(refreshTokenDto);
        when(refreshTokenRepository.findById(refreshTokenDto.memberId()))
                .thenReturn(Optional.of(refreshToken));

        // when
        RefreshTokenDto result = jwtTokenService.retrieveRefreshToken(refreshTokenValue);

        // then
        assertNotNull(result);
        assertEquals(refreshTokenDto, result);
    }

    @Test
    void retrieveAccessToken_실패() {
        // given
        String accessTokenValue = "invalidAccessToken";
        when(jwtUtil.parseAccessToken(accessTokenValue)).thenThrow(new RuntimeException());

        // when
        AccessTokenDto result = jwtTokenService.retrieveAccessToken(accessTokenValue);

        // then
        assertNull(result);
    }

    @Test
    void retrieveRefreshToken_실패() {
        // given
        String refreshTokenValue = "invalidRefreshToken";
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(1L, refreshTokenValue, 3600L);

        when(jwtUtil.parseRefreshToken(refreshTokenValue)).thenReturn(refreshTokenDto);
        when(refreshTokenRepository.findById(refreshTokenDto.memberId()))
                .thenReturn(Optional.empty());

        // when
        RefreshTokenDto result = jwtTokenService.retrieveRefreshToken(refreshTokenValue);

        // then
        assertNull(result);
    }

    @Test
    void reissueAccessTokenIfExpired_실패() {
        // given
        String accessTokenValue = "validAccessToken";
        when(jwtUtil.parseAccessToken(accessTokenValue))
                .thenReturn(new AccessTokenDto(1L, MemberRole.USER, accessTokenValue));

        // when
        AccessTokenDto result = jwtTokenService.reissueAccessTokenIfExpired(accessTokenValue);

        // then
        assertNull(result);
    }

    @Test
    void createRefreshTokenDto_성공() {
        // given
        Long memberId = fixtureMonkey.giveMeOne(Long.class);
        String tokenValue = "refreshToken";
        long ttl = 3600L;
        RefreshTokenDto refreshTokenDto = new RefreshTokenDto(memberId, tokenValue, ttl);

        when(jwtUtil.generateRefreshTokenDto(memberId)).thenReturn(refreshTokenDto);

        // when
        RefreshTokenDto result = jwtTokenService.createRefreshTokenDto(memberId);

        // then
        assertNotNull(result);
        assertEquals(refreshTokenDto, result);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void reissueAccessTokenIfExpired_성공() {
        // given
        String accessTokenValue = "expiredAccessToken";
        Long memberId = 1L;
        MemberRole memberRole = MemberRole.USER;
        AccessTokenDto accessTokenDto = new AccessTokenDto(memberId, memberRole, "newAccessToken");

        ExpiredJwtException expiredJwtException = mock(ExpiredJwtException.class);
        when(expiredJwtException.getClaims()).thenReturn(mock(io.jsonwebtoken.Claims.class));
        when(expiredJwtException.getClaims().getSubject()).thenReturn(memberId.toString());
        when(expiredJwtException.getClaims().get(TOKEN_ROLE_NAME, String.class))
                .thenReturn(memberRole.name());

        when(jwtUtil.parseAccessToken(accessTokenValue)).thenThrow(expiredJwtException);
        when(jwtUtil.generateAccessTokenDto(memberId, memberRole)).thenReturn(accessTokenDto);

        // when
        AccessTokenDto result = jwtTokenService.reissueAccessTokenIfExpired(accessTokenValue);

        // then
        assertNotNull(result);
        assertEquals(accessTokenDto, result);
    }

    @Test
    void deleteRefreshToken_성공() {
        // given
        Long memberId = fixtureMonkey.giveMeOne(Long.class);

        // when
        jwtTokenService.deleteRefreshToken(memberId);

        // then
        verify(refreshTokenRepository).deleteById(memberId);
    }
}
