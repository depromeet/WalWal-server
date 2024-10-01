package com.depromeet.stonebed.global.util;

import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MemberUtil {

    private final SecurityUtil securityUtil;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member getCurrentMember() {
        return memberRepository
                .findById(securityUtil.getCurrentMemberId())
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public Member getMemberByMemberId(Long memberId) {
        return memberRepository
                .findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }

    public String getMemberRole() {
        String role = securityUtil.getCurrentMemberRole();
        if (role == null) {
            throw new CustomException(ErrorCode.AUTH_NOT_FOUND);
        }
        return role;
    }

    @Transactional(readOnly = true)
    public void checkNickname(NicknameCheckRequest request, Member currentMember) {
        validateNicknameNotDuplicate(request.nickname(), currentMember.getProfile().getNickname());
        if (validateNicknameText(request.nickname())) {
            throw new CustomException(ErrorCode.MEMBER_INVALID_NICKNAME);
        }
    }

    private boolean validateNicknameText(String nickname) {
        return nickname == null || nickname.length() < 2 || nickname.length() > 14;
    }

    private void validateNicknameNotDuplicate(String nickname, String currentNickname) {
        if (memberRepository.existsByProfileNickname(nickname, currentNickname)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_NICKNAME);
        }
    }

    public Member getMemberByNickname(String nickname) {
        return memberRepository
                .findByProfileNickname(nickname)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
}
