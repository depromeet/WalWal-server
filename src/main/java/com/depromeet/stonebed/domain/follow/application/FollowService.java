package com.depromeet.stonebed.domain.follow.application;

import com.depromeet.stonebed.domain.follow.dao.FollowRepository;
import com.depromeet.stonebed.domain.follow.domain.Follow;
import com.depromeet.stonebed.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.stonebed.domain.follow.dto.request.FollowDeleteRequest;
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
        final Member currentMember = memberUtil.getCurrentMember();
        validateSelfFollow(currentMember.getId(), request.targetId());
        Member targetMember = getTargetMember(request.targetId());
        boolean existFollowRelation =
                followRepository.existsBySourceIdAndTargetId(
                        currentMember.getId(), targetMember.getId());

        if (existFollowRelation) {
            throw new CustomException(ErrorCode.FOLLOW_ALREADY_EXIST);
        }

        Follow follow = Follow.createFollowRelation(currentMember, targetMember);

        followRepository.save(follow);
    }

    public FollowerDeletedResponse deleteFollow(FollowDeleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(request.targetId());

        Follow follow =
                followRepository
                        .findBySourceAndTarget(currentMember, targetMember)
                        .orElseThrow(() -> new CustomException(ErrorCode.FOLLOW_NOT_EXIST));

        followRepository.delete(follow);

        return FollowerDeletedResponse.from(FollowStatus.NOT_FOLLOWING);
    }

    @Transactional(readOnly = true)
    public FollowRelationMemberResponse findFollowRelationByTargetId(Long targetId) {
        final Member currentMember = memberUtil.getCurrentMember();
        Member targetMember = getTargetMember(targetId);
        Optional<Follow> bySourceAndTarget =
                followRepository.findBySourceAndTarget(currentMember, targetMember);

        FollowStatus followStatus =
                bySourceAndTarget.isPresent() ? FollowStatus.FOLLOWING : FollowStatus.NOT_FOLLOWING;
        return FollowRelationMemberResponse.from(followStatus);
    }

    private Member getTargetMember(Long targetId) {
        Member targetMember =
                memberRepository
                        .findById(targetId)
                        .orElseThrow(
                                () ->
                                        new CustomException(
                                                ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND));
        return targetMember;
    }

    private void validateSelfFollow(Long expectedId, Long actualId) {
        if (expectedId.equals(actualId)) {
            throw new CustomException(ErrorCode.FOLLOW_SELF_NOT_ALLOWED);
        }
    }
}
