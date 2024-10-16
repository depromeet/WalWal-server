package com.depromeet.stonebed.domain.fcm.dao;

import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.MemberStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FcmTokenRepository
        extends JpaRepository<FcmToken, Long>, FcmTokenRepositoryCustom {
    Optional<FcmToken> findByMember(Member member);

    Optional<FcmToken> findByToken(String token);

    List<FcmToken> findAllByMemberStatus(MemberStatus status);

    List<FcmToken> findAllByUpdatedAtBefore(LocalDateTime cutoffDate);

    void deleteByToken(String token);

    // Delete
    @Modifying
    @Query("DELETE FROM FcmToken ft WHERE ft.member.id = :memberId")
    void deleteAllByMember(Long memberId);
}
