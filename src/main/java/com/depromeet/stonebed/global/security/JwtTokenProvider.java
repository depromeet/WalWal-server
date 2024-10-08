package com.depromeet.stonebed.global.security;

import static com.depromeet.stonebed.global.common.constants.SecurityConstants.*;

import com.depromeet.stonebed.domain.auth.dao.RefreshTokenRepository;
import com.depromeet.stonebed.domain.auth.domain.RefreshToken;
import com.depromeet.stonebed.domain.auth.dto.AccessTokenDto;
import com.depromeet.stonebed.domain.auth.dto.RefreshTokenDto;
import com.depromeet.stonebed.domain.auth.dto.response.TokenPairResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.global.util.JwtUtil;
import io.jsonwebtoken.ExpiredJwtException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenPairResponse generateTokenPair(Long memberId, MemberRole memberRole) {
        String accessToken = createAccessToken(memberId, memberRole);
        String refreshToken = createRefreshToken(memberId);
        return TokenPairResponse.of(accessToken, refreshToken);
    }

    public TokenPairResponse generateTemporaryTokenPair(Member temporaryMember) {
        String accessToken = createAccessToken(temporaryMember.getId(), MemberRole.TEMPORARY);
        String refreshToken = createRefreshToken(temporaryMember.getId());
        return TokenPairResponse.of(accessToken, refreshToken);
    }

    private String createAccessToken(Long memberId, MemberRole memberRole) {
        return jwtUtil.generateAccessToken(memberId, memberRole);
    }

    public AccessTokenDto createAccessTokenDto(Long memberId, MemberRole memberRole) {
        return jwtUtil.generateAccessTokenDto(memberId, memberRole);
    }

    private String createRefreshToken(Long memberId) {
        String token = jwtUtil.generateRefreshToken(memberId);
        saveRefreshTokenToRedis(memberId, token, jwtUtil.getRefreshTokenExpirationTime());
        return token;
    }

    public RefreshTokenDto createRefreshTokenDto(Long memberId) {
        RefreshTokenDto refreshTokenDto = jwtUtil.generateRefreshTokenDto(memberId);
        saveRefreshTokenToRedis(memberId, refreshTokenDto.tokenValue(), refreshTokenDto.ttl());
        return refreshTokenDto;
    }

    private void saveRefreshTokenToRedis(Long memberId, String refreshTokenDto, Long ttl) {
        RefreshToken refreshToken =
                RefreshToken.builder().memberId(memberId).token(refreshTokenDto).ttl(ttl).build();
        refreshTokenRepository.save(refreshToken);
    }

    public AccessTokenDto retrieveAccessToken(String accessTokenValue) {
        try {
            return jwtUtil.parseAccessToken(accessTokenValue);
        } catch (Exception e) {
            return null;
        }
    }

    public RefreshTokenDto retrieveRefreshToken(String refreshTokenValue) {
        RefreshTokenDto refreshTokenDto = parseRefreshToken(refreshTokenValue);

        if (refreshTokenDto == null) {
            return null;
        }

        // 파싱된 DTO와 일치하는 토큰이 Redis에 저장되어 있는지 확인
        Optional<RefreshToken> refreshToken = getRefreshTokenFromRedis(refreshTokenDto.memberId());

        // Redis에 토큰이 존재하고, 쿠키의 토큰과 값이 일치하면 DTO 반환
        if (refreshToken.isPresent()) {
            return refreshTokenDto;
        }

        // Redis에 토큰이 존재하지 않거나, 쿠키의 토큰과 값이 일치하지 않으면 null 반환
        return null;
    }

    private Optional<RefreshToken> getRefreshTokenFromRedis(Long memberId) {
        return refreshTokenRepository.findById(memberId);
    }

    private RefreshTokenDto parseRefreshToken(String refreshTokenValue) {
        try {
            return jwtUtil.parseRefreshToken(refreshTokenValue);
        } catch (Exception e) {
            return null;
        }
    }

    public AccessTokenDto reissueAccessTokenIfExpired(String accessTokenValue) {
        // AT가 만료된 경우 AT 재발급, 만료되지 않은 경우 null 반환
        try {
            jwtUtil.parseAccessToken(accessTokenValue);
            return null;
        } catch (ExpiredJwtException e) {
            Long memberId = Long.parseLong(e.getClaims().getSubject());
            MemberRole memberRole =
                    MemberRole.valueOf(e.getClaims().get(TOKEN_ROLE_NAME, String.class));
            return createAccessTokenDto(memberId, memberRole);
        }
    }

    public void deleteRefreshToken(Long memberId) {
        refreshTokenRepository.deleteById(memberId);
    }
}
