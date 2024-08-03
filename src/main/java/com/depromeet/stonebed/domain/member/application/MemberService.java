package com.depromeet.stonebed.domain.member.application;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.member.dto.request.CreateMemberRequest;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final MemberRepository memberRepository;
    private final MemberUtil memberUtil;

    @Transactional(readOnly = true)
    public Member findMemberInfo() {
        return memberUtil.getCurrentMember();
    }

    @Transactional(readOnly = true)
    public Optional<Member> getMemberByOauthId(OAuthProvider oAuthProvider, String oauthId) {
        return memberRepository.findByOauthInfoOauthProviderAndOauthInfoOauthId(
                oAuthProvider.getValue(), oauthId);
    }

    public Member registerMember(Member member, CreateMemberRequest request) {
        member.updateProfile(
                Profile.createProfile(
                        request.nickname(), member.getProfile().getProfileImageUrl()));
        member.updateRaisePet(request.raisePet());
        member.updateMemberRole(MemberRole.USER);
        return member;
    }

    public Member socialSignUp(OAuthProvider oAuthProvider, String oauthId, String email) {
        Member member = Member.createOAuthMember(oAuthProvider, oauthId, email);
        return memberRepository.save(member);
    }
}
