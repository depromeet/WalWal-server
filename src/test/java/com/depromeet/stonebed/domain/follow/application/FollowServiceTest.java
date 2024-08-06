package com.depromeet.stonebed.domain.follow.application;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class FollowServiceTest {

    @InjectMocks private FollowService followService;

    @Mock private MemberUtil memberUtil;
    @Mock private FollowRepository followRepository;
    @Mock private MemberRepository memberRepository;

    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setUp() {
        fixtureMonkey =
                FixtureMonkey.builder()
                        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build();
    }

    @Test
    void 팔로우를_진행합니다() {
        // given
        Member currentMember = fixtureMonkey.giveMeOne(Member.class);
        Member targetMember = fixtureMonkey.giveMeOne(Member.class);
        FollowCreateRequest request = new FollowCreateRequest(targetMember.getId());

        when(memberUtil.getCurrentMember()).thenReturn(currentMember);
        when(memberRepository.findById(targetMember.getId())).thenReturn(Optional.of(targetMember));
        when(followRepository.existsBySourceIdAndTargetId(
                        currentMember.getId(), targetMember.getId()))
                .thenReturn(false);
        when(followRepository.save(any(Follow.class)))
                .thenReturn(Follow.createFollowRelation(currentMember, targetMember));

        // when
        followService.createFollow(request);

        // then
        verify(memberUtil).getCurrentMember();
        verify(memberRepository).findById(targetMember.getId());
        verify(followRepository)
                .existsBySourceIdAndTargetId(currentMember.getId(), targetMember.getId());
        verify(followRepository).save(any(Follow.class));
    }

    @Test
    void 팔로우를_삭제합니다() {
        // given
        Member currentMember = fixtureMonkey.giveMeOne(Member.class);
        Member targetMember = fixtureMonkey.giveMeOne(Member.class);
        Follow follow = Follow.createFollowRelation(currentMember, targetMember);

        when(memberUtil.getCurrentMember()).thenReturn(currentMember);
        when(memberRepository.findById(targetMember.getId())).thenReturn(Optional.of(targetMember));
        when(followRepository.findBySourceAndTarget(currentMember, targetMember))
                .thenReturn(Optional.of(follow));

        // when
        FollowerDeletedResponse response = followService.deleteFollow(targetMember.getId());

        // then
        assertEquals(FollowStatus.NOT_FOLLOWING, response.followStatus());
        verify(memberUtil).getCurrentMember();
        verify(memberRepository).findById(targetMember.getId());
        verify(followRepository).findBySourceAndTarget(currentMember, targetMember);
        verify(followRepository).delete(follow);
    }

    @Test
    void 본인은_팔로우할_수_없습니다() {
        // given
        Member currentMember = fixtureMonkey.giveMeOne(Member.class);
        FollowCreateRequest request = new FollowCreateRequest(currentMember.getId());

        when(memberUtil.getCurrentMember()).thenReturn(currentMember);

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> followService.createFollow(request));
        assertEquals(ErrorCode.FOLLOW_SELF_NOT_ALLOWED, exception.getErrorCode());

        verify(memberUtil).getCurrentMember();
    }

    @Test
    void 타켓_유저를_찾을_수_없습니다() {
        // given
        Member currentMember = fixtureMonkey.giveMeOne(Member.class);
        Long targetId = 1L;

        when(memberUtil.getCurrentMember()).thenReturn(currentMember);
        when(memberRepository.findById(targetId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> followService.deleteFollow(targetId));
        assertEquals(ErrorCode.FOLLOW_TARGET_MEMBER_NOT_FOUND, exception.getErrorCode());

        verify(memberUtil).getCurrentMember();
        verify(memberRepository).findById(targetId);
    }

    @Test
    void 팔로우_관계가_이미_존재합니다() {
        // given
        Member currentMember = fixtureMonkey.giveMeOne(Member.class);
        Member targetMember = fixtureMonkey.giveMeOne(Member.class);
        FollowCreateRequest request = new FollowCreateRequest(targetMember.getId());

        when(memberUtil.getCurrentMember()).thenReturn(currentMember);
        when(memberRepository.findById(targetMember.getId())).thenReturn(Optional.of(targetMember));
        when(followRepository.existsBySourceIdAndTargetId(
                        currentMember.getId(), targetMember.getId()))
                .thenReturn(true);

        // when & then
        CustomException exception =
                assertThrows(CustomException.class, () -> followService.createFollow(request));
        assertEquals(ErrorCode.FOLLOW_ALREADY_EXIST, exception.getErrorCode());

        verify(memberUtil).getCurrentMember();
        verify(memberRepository).findById(targetMember.getId());
        verify(followRepository)
                .existsBySourceIdAndTargetId(currentMember.getId(), targetMember.getId());
    }

    @Test
    void 팔로우_관계를_조회합니다() {
        // given
        Member currentMember = fixtureMonkey.giveMeOne(Member.class);
        Member targetMember = fixtureMonkey.giveMeOne(Member.class);
        Follow follow = Follow.createFollowRelation(currentMember, targetMember);

        when(memberUtil.getCurrentMember()).thenReturn(currentMember);
        when(memberRepository.findById(targetMember.getId())).thenReturn(Optional.of(targetMember));
        when(followRepository.findBySourceAndTarget(currentMember, targetMember))
                .thenReturn(Optional.of(follow));

        // when
        FollowRelationMemberResponse response =
                followService.findFollowRelationByTargetId(targetMember.getId());

        // then
        assertEquals(FollowStatus.FOLLOWING, response.followStatus());
        verify(memberUtil).getCurrentMember();
        verify(memberRepository).findById(targetMember.getId());
        verify(followRepository).findBySourceAndTarget(currentMember, targetMember);
    }

    @Test
    void 팔로우_관계를_조회하는데_팔로우가_존재하지_않는_경우입니다() {
        // given
        Member currentMember = fixtureMonkey.giveMeOne(Member.class);
        Member targetMember = fixtureMonkey.giveMeOne(Member.class);

        when(memberUtil.getCurrentMember()).thenReturn(currentMember);
        when(memberRepository.findById(targetMember.getId())).thenReturn(Optional.of(targetMember));
        when(followRepository.findBySourceAndTarget(currentMember, targetMember))
                .thenReturn(Optional.empty());

        // when
        FollowRelationMemberResponse response =
                followService.findFollowRelationByTargetId(targetMember.getId());

        // then
        assertEquals(FollowStatus.NOT_FOLLOWING, response.followStatus());
        verify(memberUtil).getCurrentMember();
        verify(memberRepository).findById(targetMember.getId());
        verify(followRepository).findBySourceAndTarget(currentMember, targetMember);
    }
}
