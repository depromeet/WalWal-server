package com.depromeet.stonebed.domain.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.mission.dao.mission.MissionRepository;
import com.depromeet.stonebed.domain.mission.dao.missionHistory.MissionHistoryRepository;
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
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class MissionServiceTest {

    @Mock private MissionRepository missionRepository;
    @Mock private MissionHistoryRepository missionHistoryRepository;

    @InjectMocks private MissionService missionService;

    private LocalDate today;
    private LocalDate beforeDayByStandard;
    private Mission mission;
    private MissionHistory missionHistory;

    @BeforeEach
    public void setUp() {
        today = LocalDate.now();
        beforeDayByStandard = LocalDate.now().minusDays(3);
        mission = Mission.builder().title("Test Mission").build();
        missionHistory = MissionHistory.createMissionHistory(mission, today);
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 미션_생성_성공() {
        // Given
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
    void 미션_단일_조회_성공() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(mission));

        // When
        MissionGetOneResponse missionGetOneResponse = missionService.getMission(1L);

        // Then
        assertThat(missionGetOneResponse.title()).isEqualTo("Test Mission");
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    void 오늘의_미션_조회_성공_히스토리가_이미_존재하는_경우() {
        // Given
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
    void 오늘의_미션_조회_성공_히스토리가_없는_경우() {
        // Given: 초기 설정
        List<Mission> recentMissions = new ArrayList<>();
        recentMissions.add(Mission.builder().title("1일 전 미션").build());
        recentMissions.add(Mission.builder().title("2일 전 미션").build());
        recentMissions.add(Mission.builder().title("3일 전 미션").build());

        List<Mission> availableMissions = new ArrayList<>();
        availableMissions.add(Mission.builder().title("4일 전 미션").build());
        availableMissions.add(Mission.builder().title("5일 전 미션").build());

        when(missionRepository.findMissionsAssignedAfter(beforeDayByStandard))
                .thenReturn(recentMissions);

        when(missionRepository.findNotInMissions(recentMissions)).thenReturn(availableMissions);

        when(missionHistoryRepository.save(any(MissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: getOrCreateTodayMission 을 호출하면
        MissionGetTodayResponse result = missionService.getOrCreateTodayMission();

        // Then: 각 메서드들이 실행됐는지 검증
        assertThat(result).isNotNull();
        assertThat(result.title()).isIn("4일 전 미션", "5일 전 미션");
    }

    @Test
    void 오늘의_미션_조회_실패_할당가능한_미션이_없는_경우() {
        // Given: 초기 설정
        List<Mission> emptyMissionList = new ArrayList<>();

        when(missionRepository.findMissionsAssignedAfter(today)).thenReturn(emptyMissionList);

        when(missionRepository.findNotInMissions(emptyMissionList)).thenReturn(emptyMissionList);

        when(missionHistoryRepository.save(any(MissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: getOrCreateTodayMission 을 호출하면
        CustomException exception =
                assertThrows(CustomException.class, () -> missionService.getOrCreateTodayMission());

        // Then: 에러코드 검증
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_AVAILABLE_TODAY_MISSION);
    }

    @Test
    void 미션_조회_미션이_없는_경우() {
        // Given

        // When
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(CustomException.class, () -> missionService.getMission(1L));
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    void 미션_수정_성공() {
        // Given
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
    void 미션_수정_실패_미션이_없는_경우() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());
        MissionUpdateRequest updateRequest = new MissionUpdateRequest("Test Mission");

        // When & Then
        assertThrows(CustomException.class, () -> missionService.updateMission(1L, updateRequest));
    }

    @Test
    void 미션_삭제_성공() {
        // Given
        doNothing().when(missionRepository).deleteById(anyLong());

        // When
        missionService.deleteMission(1L);

        // Then
        verify(missionRepository, times(1)).deleteById(anyLong());
    }
}
