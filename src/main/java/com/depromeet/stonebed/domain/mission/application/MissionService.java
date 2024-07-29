package com.depromeet.stonebed.domain.mission.application;

import com.depromeet.stonebed.domain.mission.dao.MissionHistoryRepository;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetOneResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetTodayResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;
    private final MissionHistoryRepository missionHistoryRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public MissionCreateResponse createMission(MissionCreateRequest missionCreateRequest) {
        Mission mission = Mission.builder().title(missionCreateRequest.title()).build();

        mission = missionRepository.save(mission);
        return MissionCreateResponse.from(mission);
    }

    @Transactional(readOnly = true)
    public MissionGetOneResponse getMission(Long missionId) {
        return missionRepository
                .findById(missionId)
                .map(MissionGetOneResponse::from)
                .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));
    }

    @Transactional
    public MissionGetTodayResponse getOrCreateTodayMission() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        Optional<MissionHistory> optionalMissionHistory =
                missionHistoryRepository.findByAssignedDate(today);

        if (optionalMissionHistory.isPresent()) {
            return MissionGetTodayResponse.from(optionalMissionHistory.get().getMission());
        }

        // 최근 3일 이전의 미션들 불러오기
        List<Mission> recentMissions = missionRepository.findMissionsAssignedBefore(threeDaysAgo);

        // 최근 3일 이내의 미션은 제외하고 불러오기
        List<Mission> availableMissions = missionRepository.findNotInMissions(recentMissions);

        if (availableMissions.isEmpty()) {
            throw new CustomException(ErrorCode.NO_AVAILABLE_TODAY_MISSION);
        }

        Mission selectedMission =
                availableMissions.get(secureRandom.nextInt(availableMissions.size()));

        MissionHistory newMissionHistory =
                MissionHistory.builder().mission(selectedMission).assignedDate(today).build();

        missionHistoryRepository.save(newMissionHistory);

        return MissionGetTodayResponse.from(selectedMission);
    }

    public MissionUpdateResponse updateMission(
            Long missionId, MissionUpdateRequest missionUpdateRequest) {
        Mission missionToUpdate =
                missionRepository
                        .findById(missionId)
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_NOT_FOUND));

        missionToUpdate.updateTitle(missionUpdateRequest.title());
        missionRepository.save(missionToUpdate);

        return MissionUpdateResponse.from(missionToUpdate);
    }

    public void deleteMission(Long missionId) {
        missionRepository.deleteById(missionId);
    }
}
