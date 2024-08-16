package com.depromeet.stonebed.domain.missionRecord.application;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.dao.missionHistory.MissionHistoryRepository;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarDto;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCompleteTotal;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class MissionRecordService {

    private final MissionRecordRepository missionRecordRepository;
    private final MissionHistoryRepository missionHistoryRepository;
    private final MemberUtil memberUtil;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final LocalDateTime endOfYesterday =
            LocalDate.now().minusDays(1).atTime(23, 59, 59);

    public void startMission(Long missionId) {
        final Member member = memberUtil.getCurrentMember();

        MissionHistory missionHistory = findMissionHistoryById(missionId);

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

        missionRecordRepository.save(missionRecord);
    }

    public void saveMission(Long missionId, String text) {
        final Member member = memberUtil.getCurrentMember();

        MissionHistory missionHistory = findMissionHistoryById(missionId);

        MissionRecord missionRecord =
                missionRecordRepository
                        .findByMemberAndMissionHistory(member, missionHistory)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        missionRecord.updateText(text);
        missionRecord.updateStatus(MissionRecordStatus.COMPLETED);

        missionRecordRepository.save(missionRecord);
    }

    public void deleteMissionRecord(Long recordId) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        missionRecordRepository.delete(missionRecord);
    }

    private MissionHistory findMissionHistoryById(Long missionId) {
        return missionHistoryRepository
                .findLatestOneByMissionId(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_HISTORY_NOT_FOUNT));
    }

    @Transactional(readOnly = true)
    public MissionRecordCalendarResponse getMissionRecordsForCalendar(
            String cursor, int limit, Long memberId) {
        Long findMemberId =
                Optional.ofNullable(memberId)
                        .orElseGet(() -> memberUtil.getCurrentMember().getId());

        Pageable pageable = createPageable(limit);
        List<MissionRecord> records = getMissionRecords(cursor, findMemberId, pageable);
        List<MissionRecordCalendarDto> calendarData = convertToCalendarDto(records);
        String nextCursor = getNextCursor(records);

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

    private String getNextCursor(List<MissionRecord> records) {
        if (records.isEmpty()) {
            return null;
        }

        MissionRecord lastRecord = records.get(records.size() - 1);
        LocalDate nextCursorDate = lastRecord.getCreatedAt().toLocalDate().plusDays(1);
        return nextCursorDate.format(DATE_FORMATTER);
    }

    @Transactional(readOnly = true)
    public MissionTabResponse getMissionTabStatus(Long missionId) {
        final Member member = memberUtil.getCurrentMember();

        MissionHistory missionHistory = findMissionHistoryById(missionId);

        MissionRecord missionRecord =
                missionRecordRepository
                        .findByMemberAndMissionHistory(member, missionHistory)
                        .orElse(null);

        if (missionRecord == null) {
            return new MissionTabResponse(null, null, MissionRecordStatus.NOT_COMPLETED);
        }

        MissionRecordStatus missionRecordStatus = missionRecord.getStatus();
        String imageUrl =
                missionRecordStatus == MissionRecordStatus.COMPLETED
                        ? missionRecord.getImageUrl()
                        : null;

        return new MissionTabResponse(missionRecord.getId(), imageUrl, missionRecordStatus);
    }

    @Transactional
    public void updateMissionRecordWithImage(Long recordId, String imageUrl) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        missionRecord.updateImageUrl(imageUrl);
    }

    @Transactional(readOnly = true)
    public MissionRecordCompleteTotal getTotalMissionRecords() {
        final Member member = memberUtil.getCurrentMember();
        Long totalCount =
                missionRecordRepository.countByMemberIdAndStatus(
                        member.getId(), MissionRecordStatus.COMPLETED);

        return MissionRecordCompleteTotal.of(totalCount);
    }

    public void updateExpiredMissionsToNotCompleted() {
        missionRecordRepository.updateExpiredMissionsToNotCompleted(endOfYesterday);
    }
}
