package com.depromeet.stonebed.domain.member.application;

import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.member.dto.request.MemberProfileUpdateRequest;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.stonebed.global.util.MemberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberUtil memberUtil;
    private final MemberRepository memberRepository;

    @Transactional(readOnly = true)
    public Member findMemberInfo() {
        return memberUtil.getCurrentMember();
    }

    @Transactional(readOnly = true)
    public void checkNickname(NicknameCheckRequest request) {
        memberUtil.checkNickname(request);
    }

    public void modifyMemberProfile(MemberProfileUpdateRequest request) {
        Member member = memberUtil.getCurrentMember();
        Profile profile = Profile.createProfile(request.nickname(), request.profileImageUrl());
        member.updateProfile(profile);
    }
}
