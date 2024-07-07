package com.depromeet.stonebed.domain.auth.dto.response;

import com.depromeet.stonebed.domain.member.domain.Member;
import io.swagger.v3.oas.annotations.media.Schema;

public record SocialLoginResponse(
        @Schema(description = "멤버 ID", defaultValue = "1") Long memberId,
        @Schema(description = "엑세스 토큰", defaultValue = "accessToken") String accessToken,
        @Schema(description = "리프레시 토큰", defaultValue = "refreshToken") String refreshToken) {
    public static SocialLoginResponse of(Member member, TokenPairResponse tokenPairResponse) {
        return new SocialLoginResponse(
                member.getId(), tokenPairResponse.accessToken(), tokenPairResponse.refreshToken());
    }
}
