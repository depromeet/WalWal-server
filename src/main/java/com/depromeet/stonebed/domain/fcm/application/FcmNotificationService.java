package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmNotificationRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.FcmNotificationUtil;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.google.firebase.messaging.Notification;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FcmNotificationService {
    private final FcmService fcmService;
    private final FcmNotificationRepository notificationRepository;
    private final MissionRecordBoostRepository missionRecordBoostRepository;
    private final FcmRepository fcmRepository;
    private final MemberUtil memberUtil;

    public void saveNotification(
            FcmNotificationType type,
            String title,
            String message,
            String imageUrl,
            Long targetId,
            Boolean isRead) {
        final Member member = memberUtil.getCurrentMember();

        FcmNotification notification =
                new FcmNotification(type, title, message, imageUrl, member, targetId, isRead);
        notificationRepository.save(notification);
    }

    public List<FcmNotificationResponse> getNotificationsForCurrentMember() {
        final Member member = memberUtil.getCurrentMember();
        return notificationRepository.findAllByMember(member).stream()
                .map(FcmNotificationResponse::from)
                .toList();
    }

    public void checkAndSendBoostNotification(MissionRecord missionRecord) {
        Long totalBoostCount =
                missionRecordBoostRepository.sumBoostCountByMissionRecord(missionRecord.getId());
        Long recordId = missionRecord.getId();
        String imageUrl = missionRecord.getImageUrl();
        if (totalBoostCount != null) {
            String title = "";
            String message = "";

            if (totalBoostCount == 500) {
                title = "인기쟁이";
                message = "게시물 부스터를 500개를 달성했어요!";
            } else if (totalBoostCount == 5000) {
                title = "최고 인기 달성";
                message = "인기폭발! 부스터를 5000개 달성했어요!";
            }

            if (!title.isEmpty()) {
                Notification notification = FcmNotificationUtil.buildNotification(title, message);

                String token =
                        fcmRepository
                                .findByMember(missionRecord.getMember())
                                .orElseThrow()
                                .getToken();
                fcmService.sendSingleMessage(notification, token);

                saveNotification(
                        FcmNotificationType.BOOSTER, title, message, imageUrl, recordId, false);
            }
        }
    }

    @Transactional
    public void markNotificationAsRead(Long notificationId) {
        final Member member = memberUtil.getCurrentMember();
        FcmNotification notification =
                notificationRepository
                        .findByIdAndMember(notificationId, member)
                        .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        notification.markAsRead();
        notificationRepository.save(notification);
    }
}
