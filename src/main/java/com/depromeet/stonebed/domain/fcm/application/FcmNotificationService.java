package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmNotificationRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.common.constants.FcmNotificationConstants;
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
@Transactional
public class FcmNotificationService {
    private final FcmService fcmService;
    private final FcmNotificationRepository notificationRepository;
    private final MissionRecordBoostRepository missionRecordBoostRepository;
    private final FcmNotificationConstants fcmNotificationConstants;
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
                FcmNotification.create(type, title, message, imageUrl, member, targetId, isRead);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
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
            FcmNotificationConstants notificationConstants = null;

            if (totalBoostCount == 500) {
                notificationConstants = FcmNotificationConstants.POPULAR;
            } else if (totalBoostCount == 5000) {
                notificationConstants = FcmNotificationConstants.SUPER_POPULAR;
            }

            if (notificationConstants != null) {
                Notification notification =
                        FcmNotificationUtil.buildNotification(
                                notificationConstants.getTitle(),
                                notificationConstants.getMessage());

                String token =
                        fcmRepository
                                .findByMember(missionRecord.getMember())
                                .map(FcmToken::getToken)
                                .orElseThrow(
                                        () ->
                                                new CustomException(
                                                        ErrorCode.FAILED_TO_FIND_FCM_TOKEN));

                fcmService.sendSingleMessage(notification, token);

                saveNotification(
                        FcmNotificationType.BOOSTER,
                        notificationConstants.getTitle(),
                        notificationConstants.getMessage(),
                        imageUrl,
                        recordId,
                        false);
            }
        }
    }

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
