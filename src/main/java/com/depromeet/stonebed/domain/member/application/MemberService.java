package com.depromeet.stonebed.domain.member.application;

import com.depromeet.stonebed.domain.auth.domain.OAuthProvider;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberRole;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.member.dto.request.CreateMemberRequest;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
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
                        request.nickname(),
                        (request.profileImageUrl() == null || request.profileImageUrl().isEmpty())
                                ? member.getProfile().getProfileImageUrl()
                                : request.profileImageUrl()));
        member.updateRaisePet(request.raisePet());
        member.updateMemberRole(MemberRole.USER);
        return member;
    }

    public Member socialSignUp(OAuthProvider oAuthProvider, String oauthId, String email) {
        Member member = Member.createOAuthMember(oAuthProvider, oauthId, email);
        return memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public void checkNickname(NicknameCheckRequest request) {
        validateNicknameNotDuplicate(request.nickname());
        if (validateNicknameText(request.nickname())) {
            throw new CustomException(ErrorCode.MEMBER_INVALID_NICKNAME);
        }
    }

    private boolean validateNicknameText(String nickname) {
        return nickname == null || nickname.length() < 2 || nickname.length() > 14;
    }

    private void validateNicknameNotDuplicate(String nickname) {
        if (memberRepository.existsByProfileNickname(nickname)) {
            throw new CustomException(ErrorCode.MEMBER_ALREADY_NICKNAME);
        }
    }
}
