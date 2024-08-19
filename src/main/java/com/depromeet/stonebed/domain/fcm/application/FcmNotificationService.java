package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmNotificationRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationDto;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

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
    public FcmNotificationResponse getNotificationsForCurrentMember(String cursor, int limit) {
        Member member = memberUtil.getCurrentMember();

        Pageable pageable = createPageable(limit);
        List<FcmNotification> notifications = getNotifications(cursor, member.getId(), pageable);
        List<FcmNotificationDto> notificationData = convertToNotificationDto(notifications);
        String nextCursor = getNextCursor(notifications);

        return FcmNotificationResponse.from(notificationData, nextCursor);
    }

    private Pageable createPageable(int limit) {
        return PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    private List<FcmNotificationDto> convertToNotificationDto(List<FcmNotification> notifications) {
        List<Long> targetIds =
                notifications.stream()
                        .filter(
                                notification ->
                                        notification.getType() == FcmNotificationType.BOOSTER)
                        .map(FcmNotification::getTargetId)
                        .toList();

        Map<Long, MissionRecord> missionRecordMap =
                missionRecordRepository.findByIdIn(targetIds).stream()
                        .collect(
                                Collectors.toMap(
                                        MissionRecord::getId, missionRecord -> missionRecord));

        return notifications.stream()
                .map(
                        notification -> {
                            MissionRecord missionRecord =
                                    missionRecordMap.get(notification.getTargetId());
                            return FcmNotificationDto.from(notification, missionRecord);
                        })
                .toList();
    }

    private List<FcmNotification> getNotifications(
            String cursor, Long memberId, Pageable pageable) {
        if (cursor == null) {
            return notificationRepository.findByMemberId(memberId, pageable);
        }

        try {
            LocalDateTime cursorDate = LocalDateTime.parse(cursor, DATE_FORMATTER);
            return notificationRepository.findByMemberIdAndCreatedAtLessThanEqual(
                    memberId, cursorDate, pageable);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR_DATE_FORMAT);
        }
    }

    private String getNextCursor(List<FcmNotification> notifications) {
        if (notifications.isEmpty()) {
            return null;
        }

        FcmNotification lastNotification = notifications.get(notifications.size() - 1);
        return lastNotification.getCreatedAt().format(DATE_FORMATTER);
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