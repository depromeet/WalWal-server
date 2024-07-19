package com.depromeet.stonebed.global.util;

import com.depromeet.stonebed.domain.auth.domain.TokenType;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private final SecurityUtil securityUtil;
    private final MemberRepository memberRepository;

    public Member getCurrentMember() {
        return memberRepository
                .findById(securityUtil.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public Member getMemberByMemberId(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public TokenType getMemberTokenType() {
        return securityUtil.getTokenType();
    }

    public String getMemberProvider() {
        return getCurrentMember().getOauthInfo().getOauthProvider();
    }
}
