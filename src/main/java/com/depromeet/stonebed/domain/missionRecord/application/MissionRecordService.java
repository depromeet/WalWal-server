package com.depromeet.stonebed.domain.missionRecord.application;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarDto;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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

    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MemberUtil memberUtil;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public MissionRecordCreateResponse completeMission(MissionRecordCreateRequest request) {
        Mission mission = findMissionById(request.missionId());

        final Member member = memberUtil.getCurrentMember();

        MissionRecord missionRecord =
                MissionRecord.builder()
                        .member(member)
                        .mission(mission)
                        .status(MissionStatus.COMPLETED)
                        .build();

        MissionRecord createRecord = missionRecordRepository.save(missionRecord);
        return MissionRecordCreateResponse.from(
                createRecord.getId(), createRecord.getMissionTitle());
    }

    public void deleteMissionRecord(Long recordId) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        missionRecordRepository.delete(missionRecord);
    }

    private Mission findMissionById(Long missionId) {
        return missionRepository
                .findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public MissionRecordCalendarResponse getMissionRecordsForCalendar(String cursor, int limit) {
        final Member member = memberUtil.getCurrentMember();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt"));

        List<MissionRecord> records = getMissionRecords(cursor, member, pageable);

        List<MissionRecordCalendarDto> calendarData =
                records.stream()
                        .map(record -> MissionRecordCalendarDto.from(record, DATE_FORMATTER))
                        .toList();

        String nextCursor = getNextCursor(records);

        return MissionRecordCalendarResponse.from(calendarData, nextCursor);
    }

    private List<MissionRecord> getMissionRecords(String cursor, Member member, Pageable pageable) {
        if (cursor == null) {
            return missionRecordRepository.findByMemberIdWithPagination(member.getId(), pageable);
        }

        try {
            LocalDateTime cursorDate = LocalDate.parse(cursor, DATE_FORMATTER).atStartOfDay();
            return missionRecordRepository.findByMemberIdAndCreatedAtFromWithPagination(
                    member.getId(), cursorDate, pageable);
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
}
