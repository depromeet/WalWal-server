package com.depromeet.stonebed.domain.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.mission.dao.MissionHistoryRepository;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import com.depromeet.stonebed.domain.mission.domain.QMission;
import com.depromeet.stonebed.domain.mission.domain.QMissionHistory;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetOneResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetTodayResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class MissionServiceTest {

    @Mock private JPAQueryFactory queryFactory;
    @Mock private MissionRepository missionRepository;
    @Mock private MissionHistoryRepository missionHistoryRepository;
    @Mock private SecureRandom secureRandom;

    @InjectMocks private MissionService missionService;

    private LocalDate today;
    private LocalDate threeDaysAgo;
    private Mission mission;
    private MissionHistory missionHistory;

    @BeforeEach
    public void setUp() {
        today = LocalDate.now();
        threeDaysAgo = LocalDate.now().minusDays(3);
        mission = Mission.builder().title("Test Mission").build();
        missionHistory = MissionHistory.builder().mission(mission).assignedDate(today).build();
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 미션_생성_성공() {
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
    public void 미션_단일_조회_성공() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(mission));

        // When
        MissionGetOneResponse missionGetOneResponse = missionService.getMission(1L);

        // Then
        assertThat(missionGetOneResponse.title()).isEqualTo("Test Mission");
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    public void 오늘의_미션_조회_성공_히스토리가_이미_존재하는_경우() {
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
    public void 오늘의_미션_조회_성공_히스토리가_없는_경우() {
        // Given: 초기 설정
        List<Long> recentMissionIds = new ArrayList<>();
        List<Mission> missionsThreeDaysAgo = new ArrayList<>();
        missionsThreeDaysAgo.add(Mission.builder().title("4일 전 미션").build());
        missionsThreeDaysAgo.add(Mission.builder().title("5일 전 미션").build());

        when(missionHistoryRepository.findByAssignedDate(today)).thenReturn(Optional.empty());

        QMissionHistory qMissionHistory = QMissionHistory.missionHistory;
        QMission qMission = QMission.mission;

        JPAQuery<Long> recentMissionIdsQuery = mock(JPAQuery.class);
        JPAQuery<Mission> availableMissionsQuery = mock(JPAQuery.class);

        when(queryFactory.select(qMissionHistory.mission.id)).thenReturn(recentMissionIdsQuery);
        when(recentMissionIdsQuery.from(qMissionHistory)).thenReturn(recentMissionIdsQuery);
        when(recentMissionIdsQuery.where(qMissionHistory.assignedDate.before(threeDaysAgo)))
                .thenReturn(recentMissionIdsQuery);
        when(recentMissionIdsQuery.fetch()).thenReturn(recentMissionIds);

        when(queryFactory.selectFrom(qMission)).thenReturn(availableMissionsQuery);
        when(availableMissionsQuery.where(qMission.id.notIn(recentMissionIds)))
                .thenReturn(availableMissionsQuery);
        when(availableMissionsQuery.fetch()).thenReturn(missionsThreeDaysAgo);

        when(secureRandom.nextInt(missionsThreeDaysAgo.size())).thenReturn(0);
        when(missionHistoryRepository.save(any(MissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: getOrCreateTodayMission 을 호출하면
        MissionGetTodayResponse result = missionService.getOrCreateTodayMission();

        // Then: 각 메서드들이 실행됐는지 검증
        assertThat(result).isNotNull();
        assertThat(result.title()).isIn("4일 전 미션", "5일 전 미션");
        verify(missionHistoryRepository, times(1)).findByAssignedDate(today);
        verify(queryFactory, times(1)).select(qMissionHistory.mission.id);
        verify(queryFactory, times(1)).selectFrom(qMission);
        verify(missionHistoryRepository, times(1)).save(any(MissionHistory.class));
    }

    @Test
    public void 오늘의_미션_조회_실패_할당가능한_미션이_없는_경우() {
        // Given: 초기 설정
        List<Long> emptyMissionIds = new ArrayList<>();
        List<Mission> emptyMissionList = new ArrayList<>();

        when(missionHistoryRepository.findByAssignedDate(today)).thenReturn(Optional.empty());

        QMissionHistory qMissionHistory = QMissionHistory.missionHistory;
        QMission qMission = QMission.mission;

        JPAQuery<Long> recentMissionIdsQuery = mock(JPAQuery.class);
        JPAQuery<Mission> availableMissionsQuery = mock(JPAQuery.class);

        when(queryFactory.select(qMissionHistory.mission.id)).thenReturn(recentMissionIdsQuery);
        when(recentMissionIdsQuery.from(qMissionHistory)).thenReturn(recentMissionIdsQuery);
        when(recentMissionIdsQuery.where(qMissionHistory.assignedDate.before(threeDaysAgo)))
                .thenReturn(recentMissionIdsQuery);
        when(recentMissionIdsQuery.fetch()).thenReturn(emptyMissionIds);

        when(queryFactory.selectFrom(qMission)).thenReturn(availableMissionsQuery);
        when(availableMissionsQuery.where(qMission.id.notIn(emptyMissionIds)))
                .thenReturn(availableMissionsQuery);
        when(availableMissionsQuery.fetch()).thenReturn(emptyMissionList);

        when(missionHistoryRepository.save(any(MissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: getOrCreateTodayMission 을 호출하면
        CustomException exception =
                assertThrows(CustomException.class, () -> missionService.getOrCreateTodayMission());

        // Then: 에러코드 검증
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NO_AVAILABLE_TODAY_MISSION);
    }

    @Test
    public void 미션_조회_미션이_없는_경우() {
        // Given

        // When
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Then
        assertThrows(CustomException.class, () -> missionService.getMission(1L));
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    public void 미션_수정_성공() {
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
    public void 미션_수정_실패_미션이_없는_경우() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());
        MissionUpdateRequest updateRequest = new MissionUpdateRequest("Test Mission");

        // When & Then
        assertThrows(CustomException.class, () -> missionService.updateMission(1L, updateRequest));
    }

    @Test
    public void 미션_삭제_성공() {
        // Given
        doNothing().when(missionRepository).deleteById(anyLong());

        // When
        missionService.deleteMission(1L);

        // Then
        verify(missionRepository, times(1)).deleteById(anyLong());
    }
}
