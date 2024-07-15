package com.depromeet.stonebed.domain.mission.application;

import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.dto.MissionDTO;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MissionService {
    private final MissionRepository missionRepository;

    public MissionDTO createMission(MissionCreateRequest missionCreateRequest) {
        Mission mission = Mission.builder().title(missionCreateRequest.title()).build();

        mission = missionRepository.save(mission);
        return MissionDTO.from(mission);
    }

    @Transactional(readOnly = true)
    public MissionDTO getMission(Long id) {
        return missionRepository
                .findById(id)
                .map(MissionDTO::from)
                .orElseThrow(() -> new EntityNotFoundException("Mission not found. Id: " + id));
    }

    public MissionDTO updateMission(Long id, MissionUpdateRequest missionUpdateRequest) {
        Mission missionToUpdate =
                missionRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Mission not found. Id: " + id));

        missionToUpdate.updateTitle(missionUpdateRequest.title());
        missionRepository.save(missionToUpdate);

        return MissionDTO.from(missionToUpdate);
    }

    public void deleteMission(Long id) {
        missionRepository.deleteById(id);
    }
}
