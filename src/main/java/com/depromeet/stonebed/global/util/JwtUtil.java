package com.depromeet.stonebed.global.util;

import static com.depromeet.stonebed.global.common.constants.SecurityConstants.*;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.auth.domain.TokenType;
import com.depromeet.stonebed.domain.auth.dto.AccessTokenDto;
import com.depromeet.stonebed.domain.auth.dto.RefreshTokenDto;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.infra.properties.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;
    private static final String TOKEN_TYPE_KEY_NAME = "type";
    private static final String USER_ID_KEY_NAME = "memberId";
    private static final String PROVIDER_KEY_NAME = "provider";

    public String generateAccessToken(Long memberId, MemberRole memberRole) {
        Date issuedAt = new Date();
        Date expiredAt =
                new Date(issuedAt.getTime() + jwtProperties.accessTokenExpirationMilliTime());
        return buildAccessToken(memberId, memberRole, issuedAt, expiredAt);
    }

    public AccessTokenDto generateAccessTokenDto(Long memberId, MemberRole memberRole) {
        Date issuedAt = new Date();
        Date expiredAt =
                new Date(issuedAt.getTime() + jwtProperties.accessTokenExpirationMilliTime());
        String tokenValue = buildAccessToken(memberId, memberRole, issuedAt, expiredAt);
        return new AccessTokenDto(memberId, memberRole, tokenValue);
    }

    public String generateRefreshToken(Long memberId) {
        Date issuedAt = new Date();
        Date expiredAt =
                new Date(issuedAt.getTime() + jwtProperties.refreshTokenExpirationMilliTime());
        return buildRefreshToken(memberId, issuedAt, expiredAt);
    }

    public RefreshTokenDto generateRefreshTokenDto(Long memberId) {
        Date issuedAt = new Date();
        Date expiredAt =
                new Date(issuedAt.getTime() + jwtProperties.refreshTokenExpirationMilliTime());
        String tokenValue = buildRefreshToken(memberId, issuedAt, expiredAt);
        return new RefreshTokenDto(
                memberId, tokenValue, jwtProperties.refreshTokenExpirationTime());
    }

    public AccessTokenDto parseAccessToken(String token) throws ExpiredJwtException {
        // 토큰 파싱하여 성공하면 AccessTokenDto 반환, 실패하면 null 반환
        // 만료된 토큰인 경우에만 ExpiredJwtException 발생
        try {
            Jws<Claims> claims = getClaims(token, getAccessTokenKey());

            return new AccessTokenDto(
                    Long.parseLong(claims.getBody().getSubject()),
                    MemberRole.valueOf(claims.getBody().get("role", String.class)),
                    token);
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    public RefreshTokenDto parseRefreshToken(String token) throws ExpiredJwtException {
        try {
            Jws<Claims> claims = getClaims(token, getRefreshTokenKey());

            return new RefreshTokenDto(
                    Long.parseLong(claims.getBody().getSubject()),
                    token,
                    jwtProperties.refreshTokenExpirationTime());
        } catch (ExpiredJwtException e) {
            throw e;
        } catch (Exception e) {
            return null;
        }
    }

    public long getRefreshTokenExpirationTime() {
        return jwtProperties.refreshTokenExpirationTime();
    }

    private Key getRefreshTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.refreshTokenSecret().getBytes());
    }

    private Key getAccessTokenKey() {
        return Keys.hmacShaKeyFor(jwtProperties.accessTokenSecret().getBytes());
    }

    private Jws<Claims> getClaims(String token, Key key) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    private Date generateTemporaryTokenExpiration() {
        return new Date(Long.MAX_VALUE);
    }

    public String generateTemporaryToken(OAuthProvider oAuthProvider, String oauthId) {
        return Jwts.builder()
                .setHeader(createTokenHeader(TokenType.TEMPORARY))
                .setClaims(
                        Map.of(
                                USER_ID_KEY_NAME,
                                oauthId,
                                PROVIDER_KEY_NAME,
                                oAuthProvider.getValue()))
                .setExpiration(generateTemporaryTokenExpiration())
                .signWith(getAccessTokenKey())
                .compact();
    }

    private String buildAccessToken(
            Long memberId, MemberRole memberRole, Date issuedAt, Date expiredAt) {
        return Jwts.builder()
                .setHeader(createTokenHeader(TokenType.ACCESS))
                .setSubject(memberId.toString())
                .claim(TOKEN_ROLE_NAME, memberRole.name())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(getAccessTokenKey())
                .compact();
    }

    private String buildRefreshToken(Long memberId, Date issuedAt, Date expiredAt) {
        return Jwts.builder()
                .setHeader(createTokenHeader(TokenType.REFRESH))
                .setSubject(memberId.toString())
                .setIssuedAt(issuedAt)
                .setExpiration(expiredAt)
                .signWith(getRefreshTokenKey())
                .compact();
    }

    private Map<String, Object> createTokenHeader(TokenType tokenType) {
        return Map.of(
                "typ",
                "JWT",
                "alg",
                "HS256",
                "regDate",
                System.currentTimeMillis(),
                TOKEN_TYPE_KEY_NAME,
                tokenType.getValue());
    }
}
