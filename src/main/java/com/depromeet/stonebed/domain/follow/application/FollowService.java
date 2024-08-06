package com.depromeet.stonebed.domain.follow.application;

import com.depromeet.stonebed.domain.follow.dao.FollowRepository;
import com.depromeet.stonebed.domain.follow.domain.Follow;
import com.depromeet.stonebed.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.stonebed.domain.follow.dto.response.FollowRelationMemberResponse;
import com.depromeet.stonebed.domain.follow.dto.response.FollowStatus;
import com.depromeet.stonebed.domain.follow.dto.response.FollowerDeletedResponse;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
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
public class FollowService {

    private final MemberUtil memberUtil;
    private final MemberRepository memberRepository;
    private final FollowRepository followRepository;

    public void createFollow(FollowCreateRequest request) {
        Member currentMember = memberUtil.getCurrentMember();
        Long targetId = request.targetId();
        validateSelfFollow(currentMember.getId(), targetId);

        Member targetMember = getTargetMember(targetId);
        if (isFollowRelationExist(currentMember, targetMember)) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_EXIST);
        }

        Follow follow = Follow.createFollowRelation(currentMember, targetMember);
        followRepository.save(follow);
    }

    public FollowerDeletedResponse deleteFollow(Long targetId) {
        Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(targetId);

        Follow follow =
                followRepository
                        .findBySourceAndTarget(currentMember, targetMember)
                        .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_EXIST));

        followRepository.delete(follow);
        return FollowerDeletedResponse.from(FollowStatus.NOT_FOLLOWING);
    }

    @Transactional(readOnly = true)
    public FollowRelationMemberResponse findFollowRelationByTargetId(Long targetId) {
        Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(targetId);
        Optional<Follow> followRelation =
                followRepository.findBySourceAndTarget(currentMember, targetMember);

        FollowStatus followStatus =
                followRelation.isPresent() ? FollowStatus.FOLLOWING : FollowStatus.NOT_FOLLOWING;
        return FollowRelationMemberResponse.from(followStatus);
    }

    private boolean isFollowRelationExist(Member currentMember, Member targetMember) {
        return followRepository.existsBySourceIdAndTargetId(
                currentMember.getId(), targetMember.getId());
    }

    private Member getTargetMember(Long targetId) {
        return memberRepository
                .findById(targetId)
                .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND));
    }

    private void validateSelfFollow(Long expectedId, Long actualId) {
        if (expectedId.equals(actualId)) {
            throw new CustomException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }
    }
}
