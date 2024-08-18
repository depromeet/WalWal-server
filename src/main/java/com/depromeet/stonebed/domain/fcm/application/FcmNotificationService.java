package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmNotificationRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.common.constants.FcmNotificationConstants;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.FcmNotificationUtil;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.google.firebase.messaging.Notification;
import java.util.List;
import java.util.Optional;
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
    private final MissionRecordRepository missionRecordRepository;
    private final FcmRepository fcmRepository;
    private final MemberUtil memberUtil;

    private static final long POPULAR_THRESHOLD = 500;
    private static final long SUPER_POPULAR_THRESHOLD = 5000;

    public void saveNotification(
            FcmNotificationType type, String title, String message, Long targetId, Boolean isRead) {
        final Member member = memberUtil.getCurrentMember();

        FcmNotification notification =
                FcmNotification.create(type, title, message, member, targetId, isRead);
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public List<FcmNotificationResponse> getNotificationsForCurrentMember() {
        final Member member = memberUtil.getCurrentMember();
        return notificationRepository.findAllByMember(member).stream()
                .map(
                        notification -> {
                            Optional<MissionRecord> missionRecord = Optional.empty();
                            if (notification.getType() == FcmNotificationType.BOOSTER) {
                                missionRecord =
                                        missionRecordRepository.findById(
                                                notification.getTargetId());
                            }
                            return FcmNotificationResponse.from(
                                    notification, missionRecord.orElse(null));
                        })
                .toList();
    }

    public void checkAndSendBoostNotification(MissionRecord missionRecord) {
        Long totalBoostCount =
                missionRecordBoostRepository.sumBoostCountByMissionRecord(missionRecord.getId());

        if (totalBoostCount != null) {
            FcmNotificationConstants notificationConstants =
                    determineNotificationType(totalBoostCount);

            if (notificationConstants != null) {
                sendBoostNotification(missionRecord, notificationConstants);
            }
        }
    }

    private FcmNotificationConstants determineNotificationType(Long totalBoostCount) {
        if (totalBoostCount == POPULAR_THRESHOLD) {
            return FcmNotificationConstants.POPULAR;
        } else if (totalBoostCount == SUPER_POPULAR_THRESHOLD) {
            return FcmNotificationConstants.SUPER_POPULAR;
        }
        return null;
    }

    private void sendBoostNotification(
            MissionRecord missionRecord, FcmNotificationConstants notificationConstants) {
        Notification notification =
                FcmNotificationUtil.buildNotification(
                        notificationConstants.getTitle(), notificationConstants.getMessage());

        String token =
                fcmRepository
                        .findByMember(missionRecord.getMember())
                        .map(FcmToken::getToken)
                        .orElseThrow(() -> new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN));

        fcmService.sendSingleMessage(notification, token);

        saveNotification(
                FcmNotificationType.BOOSTER,
                notificationConstants.getTitle(),
                notificationConstants.getMessage(),
                missionRecord.getId(),
                false);
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
