package com.depromeet.stonebed.domain.fcm.dao;

import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface FcmNotificationRepositoryCustom {
    List<FcmNotification> findMissionRecordNotificationByMemberPaging(
            Long memberId, LocalDateTime cursorDate, Pageable pageable);
}
