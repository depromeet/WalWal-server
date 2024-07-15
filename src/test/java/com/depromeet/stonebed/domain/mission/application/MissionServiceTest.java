package com.depromeet.stonebed.domain.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.dto.MissionDTO;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MissionServiceTest {

    @Mock private MissionRepository missionRepository;

    @InjectMocks private MissionService missionService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateMission() {
        // Given
        Mission mission = Mission.builder().title("Test Mission").build();
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);
        MissionCreateRequest missionCreateRequest = new MissionCreateRequest(1L, "Test Mission");

        // When
        MissionDTO createdMission = missionService.createMission(missionCreateRequest);

        // Then
        assertThat(createdMission.title()).isEqualTo("Test Mission");
        verify(missionRepository, times(1)).save(any(Mission.class));
    }

    @Test
    public void testGetMission() {
        // Given
        Mission mission = Mission.builder().title("Test Mission").build();
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(mission));

        // When
        MissionDTO missionDTO = missionService.getMission(1L);

        // Then
        assertThat(missionDTO.title()).isEqualTo("Test Mission");
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testUpdateMissionNotFound() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());
        MissionUpdateRequest updateRequest = new MissionUpdateRequest("Test Mission");

        // When & Then
        assertThrows(
                EntityNotFoundException.class,
                () -> missionService.updateMission(1L, updateRequest));
    }

    @Test
    public void testDeleteMission() {
        // Given
        doNothing().when(missionRepository).deleteById(anyLong());

        // When
        missionService.deleteMission(1L);

        // Then
        verify(missionRepository, times(1)).deleteById(anyLong());
    }
}
