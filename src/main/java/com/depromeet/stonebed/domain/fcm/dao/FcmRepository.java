package com.depromeet.stonebed.domain.fcm.dao;

import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface FcmRepository extends JpaRepository<FcmToken, Long> {
    Optional<FcmToken> findByMember(Member member);

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAll();

    List<FcmToken> findAllByUpdatedAtBefore(LocalDateTime cutoffDate);

    @Query("SELECT f.token FROM FcmToken f WHERE f.member.status = 'NORMAL'")
    List<String> findAllValidTokens();

    Optional<FcmToken> findByMemberAndMemberStatus(Member member, MemberStatus status);
}
