package com.depromeet.stonebed.domain.mission.application;

import com.depromeet.stonebed.domain.mission.api.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.dto.MissionDTO;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
@Transactional // 클래스 레벨에 트랜잭션 적용
public class MissionService {
    private final MissionRepository missionRepository;

    public MissionService(MissionRepository missionRepository) {
        this.missionRepository = missionRepository;
    }

    public MissionDTO createMission(MissionDTO missionDTO) {
        Mission mission = Mission.builder().title(missionDTO.getTitle()).build();

        mission = missionRepository.save(mission);
        return convertToDTO(mission);
    }

    public MissionDTO getMission(Long id) {
        return missionRepository
                .findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("Mission not found. Id: " + id));
    }

    public MissionDTO updateMission(Long id, MissionUpdateRequest missionUpdateRequest) {
        Mission missionToUpdate =
                missionRepository
                        .findById(id)
                        .orElseThrow(
                                () -> new EntityNotFoundException("Mission not found. Id: " + id));

        missionToUpdate.updateTitle(missionUpdateRequest.getTitle());
        return convertToDTO(missionToUpdate);
    }

    public void deleteMission(Long id) {
        missionRepository.deleteById(id);
    }

    private MissionDTO convertToDTO(Mission mission) {
        return MissionDTO.builder().id(mission.getId()).title(mission.getTitle()).build();
    }
}
