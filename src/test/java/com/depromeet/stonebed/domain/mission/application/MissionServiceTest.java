package com.depromeet.stonebed.domain.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

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
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MissionServiceTest {

    @Mock private MissionRepository missionRepository;
    @Mock private MissionHistoryRepository missionHistoryRepository;

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
    public void testGetOrCreateTodayMission_오늘의_미션_히스토리가_이미_존재하는_경우() {
        // Given
        LocalDate today = LocalDate.now();
        Mission mission = Mission.builder().title("Test Mission").build();
        MissionHistory missionHistory =
                MissionHistory.builder().mission(mission).assignedDate(today).build();

        when(missionHistoryRepository.findByAssignedDate(today))
                .thenReturn(Optional.of(missionHistory));

        // When
        MissionGetTodayResponse result = missionService.getOrCreateTodayMission();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo("Test Mission");
        verify(missionHistoryRepository, times(1)).findByAssignedDate(today);
        verify(missionHistoryRepository, times(0)).save(any(MissionHistory.class));
    }

    @Test
    void testGetOrCreateTodayMission_오늘의_미션_히스토리가_없는_경우() {
        // Given: 1 ~ 5일 전 데이터를 만든다
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        List<MissionHistory> recentMissionHistories = new ArrayList<>();
        List<Long> recentMissionIds = new ArrayList<>();

        // 1일 전 ~ 3일 전
        for (int i = 1; i < 4; i++) {
            LocalDate date = today.minusDays(i);
            Mission mission = spy(Mission.builder().title(String.format("%d일 전 미션", i)).build());
            MissionHistory missionHistory =
                    MissionHistory.builder().mission(mission).assignedDate(date).build();

            recentMissionHistories.add(missionHistory);
            recentMissionIds.add((long) i);
            doReturn((long) i).when(mission).getId();
        }

        // 오늘의 히스토리가 없다는 동작을 모킹
        when(missionHistoryRepository.findByAssignedDate(today)).thenReturn(Optional.empty());
        // 3일이내 미션 히스토리를 가져왔을 때 위에 1일전, 2일전, 3일전 미션히스토리를 가져옴
        when(missionHistoryRepository.findByAssignedDateBefore(threeDaysAgo))
                .thenReturn(recentMissionHistories);

        List<Mission> missionsThreeDaysAgo = new ArrayList<>();

        // 4일 전 ~ 5일 전
        for (int i = 4; i < 6; i++) {
            Mission mission = spy(Mission.builder().title(String.format("%d일 전 미션", i)).build());
            missionsThreeDaysAgo.add(mission);
        }

        // 최근 3일간 할당되지 않은 미션 가져오는 동작을 모킹
        when(missionRepository.findAllByIdNotIn(recentMissionIds)).thenReturn(missionsThreeDaysAgo);
        when(missionHistoryRepository.save(any(MissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: getOrCreateTodayMission 을 호출하면
        MissionGetTodayResponse result = missionService.getOrCreateTodayMission();

        // Then: 각 메서드들이 실행됐는지 검증
        assertThat(result).isNotNull();
        assertThat(result.title()).isIn("4일 전 미션", "5일 전 미션");
        verify(missionHistoryRepository, times(1)).findByAssignedDate(today);
        verify(missionHistoryRepository, times(1)).findByAssignedDateBefore(threeDaysAgo);
        verify(missionRepository, times(1)).findAllByIdNotIn(recentMissionIds);
        verify(missionHistoryRepository, times(1)).save(any(MissionHistory.class));
    }

    @Test
    void testGetOrCreateTodayMission_할당가능한_미션이_없는_경우() {
        LocalDate today = LocalDate.now();
        LocalDate threeDaysAgo = today.minusDays(3);

        List<MissionHistory> emptyMissionHistories = new ArrayList<>();
        List<Long> emptyMissionIds = new ArrayList<>();

        when(missionHistoryRepository.findByAssignedDate(today)).thenReturn(Optional.empty());
        when(missionHistoryRepository.findByAssignedDateBefore(threeDaysAgo))
                .thenReturn(emptyMissionHistories);

        List<Mission> emptyMissionList = new ArrayList<>();

        // 할당 가능한 미션이 없는 경우를 모킹
        when(missionRepository.findAllByIdNotIn(emptyMissionIds)).thenReturn(emptyMissionList);
        when(missionHistoryRepository.save(any(MissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: getOrCreateTodayMission 을 호출하면
        CustomException exception =
                assertThrows(CustomException.class, () -> missionService.getOrCreateTodayMission());

        // Then: 에러코드 검증
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_AVAILABLE_TODAY_MISSION);
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
