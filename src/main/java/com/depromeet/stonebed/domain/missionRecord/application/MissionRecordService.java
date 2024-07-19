package com.depromeet.stonebed.domain.missionRecord.application;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordDayRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordDayResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
    public MissionRecordCalendarResponse getMissionRecordsForCalendar() {
        Member member = memberUtil.getCurrentMember();
        List<MissionRecord> records = missionRecordRepository.findByMemberId(member.getId());

        Map<String, Map<String, List<String>>> calendarData =
                records.stream()
                        .collect(
                                Collectors.groupingBy(
                                        record ->
                                                record.getCreatedAt()
                                                        .format(
                                                                DateTimeFormatter.ofPattern(
                                                                        "yyyy-MM")),
                                        Collectors.groupingBy(
                                                record ->
                                                        record.getCreatedAt()
                                                                .format(
                                                                        DateTimeFormatter.ofPattern(
                                                                                "dd")),
                                                Collectors.mapping(
                                                        MissionRecord::getImageUrl,
                                                        Collectors.toList()))));

        return MissionRecordCalendarResponse.from(calendarData);
    }

    // 단건 미션 기록 조회 메서드
    public MissionRecordDayResponse getMissionRecordsForDay(MissionRecordDayRequest request) {
        Member member = memberUtil.getCurrentMember();
        LocalDate date = request.date();
        MissionRecord record =
                missionRecordRepository
                        .findFirstByMemberIdAndCreatedAt(member.getId(), date)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        return MissionRecordDayResponse.from(record.getImageUrl());
    }
}
