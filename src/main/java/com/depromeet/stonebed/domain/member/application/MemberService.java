package com.depromeet.stonebed.domain.member.application;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import com.depromeet.stonebed.domain.member.domain.OauthInfo;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.member.dto.CreateNewUserDTO;
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

    public Member setMemberRegister(Member member, CreateMemberRequest request) {
        member.updateProfile(Profile.createProfile(request.nickname(), request.profileImageUrl()));
        member.updateRaisePet(request.raisePet());
        member.updateMarketingAgreement(request.marketingAgreement());
        return member;
    }

    private Member createNewMember(CreateNewUserDTO createNewUserDTO) {
        return memberRepository.save(
                Member.createMember(
                        Profile.createProfile(
                                createNewUserDTO.nickname(), createNewUserDTO.profileImageUrl()),
                        OauthInfo.createOauthInfo(
                                createNewUserDTO.oauthId(),
                                createNewUserDTO.provider().getValue(),
                                createNewUserDTO.email()),
                        MemberStatus.NORMAL,
                        MemberRole.USER,
                        createNewUserDTO.raisePet(),
                        createNewUserDTO.marketingAgreement()));
    }

    public Member socialSignUp(OAuthProvider oAuthProvider, String oauthId, String email) {
        Member member = Member.createOAuthMember(oAuthProvider, oauthId, email);
        return memberRepository.save(member);
    }
}
