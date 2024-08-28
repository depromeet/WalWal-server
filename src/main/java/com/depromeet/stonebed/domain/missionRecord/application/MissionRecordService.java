package com.depromeet.stonebed.domain.missionRecord.application;

import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionHistory.dao.MissionHistoryRepository;
import com.depromeet.stonebed.domain.missionHistory.domain.MissionHistory;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordBoost;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCalendarRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarDto;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCompleteTotal;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordIdResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionRecordService {
    private final FcmNotificationService fcmNotificationService;
    private final MissionRecordRepository missionRecordRepository;
    private final MissionRepository missionRepository;
    private final MissionHistoryRepository missionHistoryRepository;
    private final MissionRecordBoostRepository missionRecordBoostRepository;
    private final MemberUtil memberUtil;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MissionRecordIdResponse startMission(Long missionId) {
        final Member member = memberUtil.getCurrentMember();
        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        MissionHistory missionHistory =
                findMissionHistoryByIdAndRaisePet(missionId, mission.getRaisePet());

        MissionRecord missionRecord =
                missionRecordRepository
                        .findByMemberAndMissionHistory(member, missionHistory)
                        .orElseGet(
                                () ->
                                        MissionRecord.builder()
                                                .member(member)
                                                .missionHistory(missionHistory)
                                                .status(MissionRecordStatus.IN_PROGRESS)
                                                .build());

        MissionRecord saveMissionRecord = missionRecordRepository.save(missionRecord);
        return MissionRecordIdResponse.of(saveMissionRecord.getId());
    }

    public void saveMission(Long missionId, String content) {
        final Member member = memberUtil.getCurrentMember();

        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        MissionHistory missionHistory =
                findMissionHistoryByIdAndRaisePet(missionId, mission.getRaisePet());

        LocalDate today = LocalDate.now();
        boolean recordExists =
                missionRecordRepository.existsByMemberAndMissionHistoryAndStatusAndCreatedAtBetween(
                        member,
                        missionHistory,
                        MissionRecordStatus.COMPLETED,
                        today.atStartOfDay(),
                        today.plusDays(1).atStartOfDay());

        if (recordExists) {
            throw new CustomException(ErrorCode.DUPLICATE_MISSION_RECORD);
        }

        MissionRecord missionRecord =
                missionRecordRepository
                        .findByMemberAndMissionHistory(member, missionHistory)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        missionRecord.updateContent(content);

        missionRecordRepository.save(missionRecord);
    }

    public void deleteMissionRecord(Long recordId) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        missionRecordRepository.delete(missionRecord);
    }

    public void createBoost(Long missionRecordId, Long boostCount) {
        Member currentMember = memberUtil.getCurrentMember();
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(missionRecordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        if (Objects.equals(currentMember.getId(), missionRecord.getMember().getId())) {
            throw new CustomException(ErrorCode.BOOST_UNAVAILABLE_MY_FEED);
        }

        MissionRecordBoost missionRecordBoost =
                MissionRecordBoost.builder()
                        .missionRecord(missionRecord)
                        .member(currentMember)
                        .count(boostCount)
                        .build();

        missionRecordBoostRepository.save(missionRecordBoost);

        fcmNotificationService.checkAndSendBoostNotification(missionRecord);
    }

    private MissionHistory findMissionHistoryByIdAndRaisePet(Long missionId, RaisePet raisePet) {
        return missionHistoryRepository
                .findLatestOneByMissionIdRaisePet(missionId, raisePet)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_HISTORY_NOT_FOUNT));
    }

    @Transactional(readOnly = true)
    public MissionRecordCalendarResponse getMissionRecordsForCalendar(
            MissionRecordCalendarRequest request) {
        Long findMemberId =
                Optional.ofNullable(request.memberId())
                        .orElseGet(() -> memberUtil.getCurrentMember().getId());

        Pageable pageable = createPageable(request.limit());
        List<MissionRecord> records = getMissionRecords(request.cursor(), findMemberId, pageable);
        List<MissionRecordCalendarDto> calendarData = convertToCalendarDto(records);
        String nextCursor = getNextCursor(records, request.limit());

        return MissionRecordCalendarResponse.from(calendarData, nextCursor);
    }

    private Pageable createPageable(int limit) {
        return PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt"));
    }

    private List<MissionRecordCalendarDto> convertToCalendarDto(List<MissionRecord> records) {
        return records.stream()
                .map(record -> MissionRecordCalendarDto.from(record, DATE_FORMATTER))
                .toList();
    }

    private List<MissionRecord> getMissionRecords(String cursor, Long memberId, Pageable pageable) {
        if (cursor == null) {
            return missionRecordRepository.findByMemberIdWithPagination(memberId, pageable);
        }

        try {
            LocalDateTime cursorDate = LocalDate.parse(cursor, DATE_FORMATTER).atStartOfDay();
            return missionRecordRepository.findByMemberIdAndCreatedAtFromWithPagination(
                    memberId, cursorDate, pageable);
        } catch (DateTimeParseException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR_DATE_FORMAT);
        }
    }

    private String getNextCursor(List<MissionRecord> records, int limit) {
        if (records.size() < limit) {
            return null;
        }
        return String.valueOf(getLastRecordId(records));
    }

    private Long getLastRecordId(List<MissionRecord> records) {
        if (records.isEmpty()) {
            return null;
        }

        return records.get(records.size() - 1).getId();
    }

    @Transactional(readOnly = true)
    public MissionTabResponse getMissionTabStatus(Long missionId) {
        final Member member = memberUtil.getCurrentMember();

        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        MissionHistory missionHistory =
                findMissionHistoryByIdAndRaisePet(missionId, mission.getRaisePet());

        MissionRecord missionRecord =
                missionRecordRepository
                        .findByMemberAndMissionHistory(member, missionHistory)
                        .orElse(null);

        if (missionRecord == null) {
            return MissionTabResponse.of(null, MissionRecordStatus.NOT_COMPLETED);
        }

        MissionRecordStatus missionRecordStatus = missionRecord.getStatus();
        String imageUrl =
                missionRecordStatus == MissionRecordStatus.COMPLETED
                        ? missionRecord.getImageUrl()
                        : null;

        return MissionTabResponse.of(imageUrl, missionRecordStatus);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateMissionRecordWithImage(Long recordId, String imageUrl) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));
        missionRecord.updateStatus(MissionRecordStatus.COMPLETED);
        missionRecord.updateImageUrl(imageUrl);
    }

    @Transactional(readOnly = true)
    public MissionRecordCompleteTotal getTotalMissionRecords(Long memberId) {
        final Member currentMember = memberUtil.getCurrentMember();
        Long findMemberId = Optional.ofNullable(memberId).orElseGet(currentMember::getId);

        Long totalCount =
                missionRecordRepository.countByMemberIdAndStatus(
                        findMemberId, MissionRecordStatus.COMPLETED);

        return MissionRecordCompleteTotal.of(totalCount);
    }

    public void expiredMissionsToNotCompletedUpdate() {
        LocalDateTime endOfYesterday = LocalDate.now().minusDays(1).atTime(23, 59, 59);
        missionRecordRepository.updateExpiredMissionsToNotCompleted(endOfYesterday);
    }
}
