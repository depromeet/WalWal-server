package com.depromeet.stonebed.domain.missionRecord.application;

import com.depromeet.stonebed.domain.image.dao.ImageRepository;
import com.depromeet.stonebed.domain.image.domain.Image;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionCompleteResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarDto;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
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
    private final ImageRepository imageRepository;
    private final MemberUtil memberUtil;

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public void startMission(Long missionId) {
        final Member member = memberUtil.getCurrentMember();

        Mission mission = findMissionById(missionId);

        MissionRecord missionRecord =
                missionRecordRepository
                        .findByMemberAndMission(member, mission)
                        .orElseGet(
                                () ->
                                        MissionRecord.builder()
                                                .member(member)
                                                .mission(mission)
                                                .status(MissionRecordStatus.IN_PROGRESS)
                                                .build());

        missionRecordRepository.save(missionRecord);
    }

    public MissionCompleteResponse getMissionImageUrl(Long recordId) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        return MissionCompleteResponse.from(missionRecord.getImageUrl());
    }

    public void saveMission(Long recordId, Long missionId) {
        final Member member = memberUtil.getCurrentMember();

        Image image =
                imageRepository
                        .findByTargetId(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_KEY_NOT_FOUND));

        Mission mission =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        String imageUrl = image.getImageKey();

        MissionRecord missionRecord =
                MissionRecord.builder()
                        .member(member)
                        .mission(mission)
                        .status(MissionRecordStatus.COMPLETED)
                        .imageUrl(imageUrl)
                        .build();

        missionRecordRepository.save(missionRecord);
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

    @Transactional(readOnly = true)
    public MissionTabResponse getMissionTabStatus(Long recordId) {
        MissionRecord missionRecord = missionRecordRepository.findById(recordId).orElse(null);

        MissionRecordStatus missionRecordStatus =
                missionRecord != null
                        ? missionRecord.getStatus()
                        : MissionRecordStatus.NOT_COMPLETED;

        return MissionTabResponse.from(missionRecordStatus);
    }

    @Transactional
    public void updateMissionRecordWithImage(Long recordId, String imageUrl) {
        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(recordId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        missionRecord.updateImageUrl(imageUrl);
    }

    public Long getTotalMissionRecords() {
        final Member member = memberUtil.getCurrentMember();
        return missionRecordRepository.countByMemberIdAndStatus(
                member.getId(), MissionRecordStatus.COMPLETED);
    }
}
