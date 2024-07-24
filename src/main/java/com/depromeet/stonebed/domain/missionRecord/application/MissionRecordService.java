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
import java.util.List;
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
public class MissionRecordService {

    private final MissionRepository missionRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MemberUtil memberUtil;

    public MissionRecordCreateResponse completeMission(MissionRecordCreateRequest request) {
        Mission mission = findMissionById(request.missionId());

        Member member = memberUtil.getCurrentMember();

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

    // 단일 미션 조회 메서드
    private Mission findMissionById(Long missionId) {
        return missionRepository
                .findById(missionId)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    // 전체 미션 기록 조회 메서드
    @Transactional(readOnly = true)
    public MissionRecordCalendarResponse getMissionRecordsForCalendar(String cursor, int limit) {
        Member member = memberUtil.getCurrentMember();
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt"));

        List<MissionRecord> records;
        if (cursor == null) {
            records = missionRecordRepository.findByMemberId(member.getId(), pageable);
        } else {
            LocalDateTime cursorDate =
                    LocalDate.parse(cursor, DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                            .atStartOfDay();
            records =
                    missionRecordRepository.findByMemberIdAndCreatedAtAfter(
                            member.getId(), cursorDate, pageable);
        }

        List<MissionRecordCalendarDto> calendarData =
                records.stream()
                        .map(record -> MissionRecordCalendarDto.from(record))
                        .collect(Collectors.toList());

        String nextCursor = null;
        if (!records.isEmpty()) {
            MissionRecord lastRecord = records.get(records.size() - 1);
            LocalDate nextCursorDate = lastRecord.getCreatedAt().toLocalDate().plusDays(1);
            nextCursor = nextCursorDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }

        return MissionRecordCalendarResponse.from(calendarData, nextCursor);
    }
}
