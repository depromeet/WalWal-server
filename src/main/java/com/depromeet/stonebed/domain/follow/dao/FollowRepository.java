package com.depromeet.stonebed.domain.follow.dao;

import com.depromeet.stonebed.domain.follow.domain.Follow;
import com.depromeet.stonebed.domain.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FollowRepository extends JpaRepository<Follow, Long> {
    boolean existsBySourceIdAndTargetId(Long sourceId, Long targetId);

    Optional<Follow> findBySourceAndTarget(Member currentMember, Member targetMember);

    List<Follow> findAllBySource(Member currentMember);
}
