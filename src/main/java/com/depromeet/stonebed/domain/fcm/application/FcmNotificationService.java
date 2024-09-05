package com.depromeet.stonebed.domain.fcm.application;

import com.depromeet.stonebed.domain.fcm.dao.FcmNotificationRepository;
import com.depromeet.stonebed.domain.fcm.dao.FcmRepository;
import com.depromeet.stonebed.domain.fcm.domain.FcmMessage;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotification;
import com.depromeet.stonebed.domain.fcm.domain.FcmNotificationType;
import com.depromeet.stonebed.domain.fcm.domain.FcmToken;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationDto;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationResponse;
import com.depromeet.stonebed.domain.member.dao.MemberRepository;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.sqs.application.SqsMessageService;
import com.depromeet.stonebed.global.common.constants.FcmNotificationConstants;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
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
    private final SqsMessageService sqsMessageService;
    private final FcmNotificationRepository notificationRepository;
    private final MissionRecordBoostRepository missionRecordBoostRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final FcmRepository fcmRepository;
    private final MemberRepository memberRepository;
    private final MemberUtil memberUtil;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    private static final long POPULAR_THRESHOLD = 500;
    private static final long SUPER_POPULAR_THRESHOLD = 5000;

    public void saveNotification(
            FcmNotificationType type,
            String title,
            String message,
            Long targetId,
            Long memberId,
            Boolean isRead) {
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

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

        boolean hasNext =
                notificationRepository.existsByCreatedAtLessThan(lastNotification.getCreatedAt());

        return hasNext ? lastNotification.getCreatedAt().format(DATE_FORMATTER) : null;
    }

    @Transactional
    public void checkAndSendBoostNotification(MissionRecord missionRecord) {
        Long totalBoostCount =
                missionRecordBoostRepository.sumBoostCountByMissionRecord(missionRecord.getId());

        if (totalBoostCount != null) {
            Optional<FcmNotificationConstants> notificationType =
                    determineNotificationType(totalBoostCount);

            notificationType.ifPresent(
                    type -> {
                        if (!notificationAlreadySent(missionRecord, type)) {
                            sendBoostNotification(missionRecord, type);
                        }
                    });
        }
    }

    private boolean notificationAlreadySent(
            MissionRecord missionRecord, FcmNotificationConstants notificationConstants) {
        return notificationRepository.existsByTargetIdAndTypeAndTitle(
                missionRecord.getId(),
                FcmNotificationType.BOOSTER,
                notificationConstants.getTitle());
    }

    private Optional<FcmNotificationConstants> determineNotificationType(Long totalBoostCount) {
        if (totalBoostCount >= SUPER_POPULAR_THRESHOLD) {
            return Optional.of(FcmNotificationConstants.SUPER_POPULAR);
        }
        if (totalBoostCount >= POPULAR_THRESHOLD) {
            return Optional.of(FcmNotificationConstants.POPULAR);
        }

        return Optional.empty();
    }

    private void sendBoostNotification(
            MissionRecord missionRecord, FcmNotificationConstants notificationConstants) {
        String token =
                fcmRepository
                        .findByMember(missionRecord.getMember())
                        .map(FcmToken::getToken)
                        .orElseThrow(() -> new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN));

        FcmMessage fcmMessage =
                FcmMessage.of(
                        notificationConstants.getTitle(),
                        notificationConstants.getMessage(),
                        token);
        sqsMessageService.sendMessage(fcmMessage);

        saveNotification(
                FcmNotificationType.BOOSTER,
                notificationConstants.getTitle(),
                notificationConstants.getMessage(),
                missionRecord.getId(),
                missionRecord.getMember().getId(),
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

    private List<FcmNotification> buildNotificationList(
            String title, String message, List<String> tokens) {
        List<FcmNotification> notifications = new ArrayList<>();

        for (String token : tokens) {
            Member member =
                    fcmRepository
                            .findByToken(token)
                            .map(FcmToken::getMember)
                            .orElseThrow(
                                    () -> new CustomException(ErrorCode.FAILED_TO_FIND_FCM_TOKEN));

            FcmNotification newNotification =
                    FcmNotification.create(
                            FcmNotificationType.MISSION, title, message, member, null, false);
            notifications.add(newNotification);
        }

        return notifications;
    }

    public void sendAndNotifications(String title, String message, List<String> tokens) {
        List<List<String>> batches = createBatches(tokens, 10);

        for (List<String> batch : batches) {
            sqsMessageService.sendBatchMessages(batch, title, message);
        }

        List<FcmNotification> notifications = buildNotificationList(title, message, tokens);
        notificationRepository.saveAll(notifications);
    }

    private List<List<String>> createBatches(List<String> tokens, int batchSize) {
        return IntStream.range(0, (tokens.size() + batchSize - 1) / batchSize)
                .mapToObj(
                        i ->
                                tokens.subList(
                                        i * batchSize,
                                        Math.min(tokens.size(), (i + 1) * batchSize)))
                .collect(Collectors.toList());
    }
}
