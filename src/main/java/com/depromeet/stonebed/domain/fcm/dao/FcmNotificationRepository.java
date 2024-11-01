package com.depromeet.stonebed.domain.fcm.dao;

import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.member.domain.Member;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FcmNotificationRepository
        extends JpaRepository<FcmNotification, Long>, FcmNotificationRepositoryCustom {
    List<FcmNotification> findByMemberId(Long memberId, Pageable pageable);

    Optional<FcmNotification> findByIdAndMember(Long id, Member member);

    boolean existsByCreatedAtLessThan(LocalDateTime createdAt);

    boolean existsByTargetIdAndTypeAndTitle(Long targetId, FcmNotificationType type, String title);

    // Delete
    @Modifying
    @Query("DELETE FROM FcmNotification fn WHERE fn.member.id = :memberId")
    void deleteAllByMember(@Param("memberId") Long memberId);
}
