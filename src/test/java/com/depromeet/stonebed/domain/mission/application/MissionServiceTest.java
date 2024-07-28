package com.depromeet.stonebed.domain.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetOneResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.stonebed.global.error.exception.CustomException;
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
        MissionCreateRequest missionCreateRequest = new MissionCreateRequest("Test Mission");

        // When
        MissionCreateResponse missionCreateResponse =
                missionService.createMission(missionCreateRequest);

        // Then
        assertThat(missionCreateResponse.title()).isEqualTo("Test Mission");
        verify(missionRepository, times(1)).save(any(Mission.class));
    }

    @Test
    public void testGetMission() {
        // Given
        Mission mission = Mission.builder().title("Test Mission").build();
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(mission));

        // When
        MissionGetOneResponse missionGetOneResponse = missionService.getMission(1L);

        // Then
        assertThat(missionGetOneResponse.title()).isEqualTo("Test Mission");
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testGetMissionNotFound() {
        // Given

        // When
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(CustomException.class, () -> missionService.getMission(1L));
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    public void testUpdateMission() {
        // Given
        Mission mission = Mission.builder().title("Test Mission").build();
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(mission));

        // When
        MissionUpdateResponse missionUpdateResponse =
                missionService.updateMission(1L, new MissionUpdateRequest("Updated Mission"));

        // Then
        assertThat(missionUpdateResponse.title()).isEqualTo("Updated Mission");
        verify(missionRepository, times(1)).findById(anyLong());
        verify(missionRepository, times(1)).save(any(Mission.class));
    }

    @Test
    public void testUpdateMissionNotFound() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());
        MissionUpdateRequest updateRequest = new MissionUpdateRequest("Test Mission");

        // When & Then
        assertThrows(CustomException.class, () -> missionService.updateMission(1L, updateRequest));
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
