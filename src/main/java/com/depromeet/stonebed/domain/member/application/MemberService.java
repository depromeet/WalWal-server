package com.depromeet.stonebed.domain.member.application;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.member.dto.request.MemberProfileUpdateRequest;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.stonebed.domain.member.dto.response.MemberInfoResponse;
import com.depromeet.stonebed.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberUtil memberUtil;

    @Transactional(readOnly = true)
    public MemberInfoResponse findMemberMyInfo() {
        Member currentMember = memberUtil.getCurrentMember();
        return MemberInfoResponse.from(currentMember);
    }

    @Transactional(readOnly = true)
    public MemberInfoResponse findMemberInfo(Long memberId) {
        Member member = memberUtil.getMemberByMemberId(memberId);
        return MemberInfoResponse.from(member);
    }

    @Transactional(readOnly = true)
    public void checkNickname(NicknameCheckRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        memberUtil.checkNickname(request, currentMember);
    }

    public void modifyMemberProfile(MemberProfileUpdateRequest request) {
        Member member = memberUtil.getCurrentMember();

        Profile profile = Profile.createProfile(request.nickname(), request.profileImageUrl());
        member.updateProfile(profile);
    }
}
