package com.depromeet.stonebed.domain.missionRecord.application;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
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
        final Mission mission =
                missionRepository
                        .findById(request.missionId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

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
                        .orElseThrow(
                                () -> {
                                    return new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND);
                                });

        missionRecordRepository.delete(missionRecord);
    }
}
