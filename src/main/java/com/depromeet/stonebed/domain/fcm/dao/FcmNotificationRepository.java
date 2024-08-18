package com.depromeet.stonebed.domain.fcm.dao;

import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.member.domain.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FcmNotificationRepository extends JpaRepository<FcmNotification, Long> {
    List<FcmNotification> findByMemberId(Long memberId, Pageable pageable);

    List<FcmNotification> findByMemberIdAndCreatedAtLessThanEqual(
            Long memberId, LocalDateTime cursorDate, Pageable pageable);

    Optional<FcmNotification> findByIdAndMember(Long id, Member member);
}
