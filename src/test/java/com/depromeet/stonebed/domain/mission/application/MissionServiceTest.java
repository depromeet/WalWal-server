package com.depromeet.stonebed.domain.mission.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetOneResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetTodayResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.stonebed.domain.missionHistory.dao.MissionHistoryRepository;
import com.depromeet.stonebed.domain.missionHistory.domain.MissionHistory;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MissionServiceTest extends FixtureMonkeySetUp {

    @Mock private MissionRepository missionRepository;
    @Mock private MissionHistoryRepository missionHistoryRepository;
    @Mock private MemberUtil memberUtil;

    @InjectMocks private MissionService missionService;

    private LocalDate today;
    private LocalDate beforeDayByStandard;
    private Mission mission;
    private MissionHistory missionHistory;
    private Member member;

    @BeforeEach
    public void setUp() {
        today = LocalDate.now();
        beforeDayByStandard = LocalDate.now().minusDays(3);
        mission =
                fixtureMonkey
                        .giveMeBuilder(Mission.class)
                        .set("raisePet", RaisePet.DOG)
                        .build()
                        .sample();
        missionHistory = MissionHistory.createMissionHistory(mission, today, RaisePet.DOG);
        member = mock(Member.class);
    }

    @Test
    void 미션_생성_성공() {
        // Given
        when(missionRepository.save(any(Mission.class))).thenReturn(mission);
        MissionCreateRequest missionCreateRequest =
                new MissionCreateRequest(
                        mission.getTitle(), mission.getRaisePet(), mission.getCompleteMessage());

        // When
        MissionCreateResponse missionCreateResponse =
                missionService.createMission(missionCreateRequest);

        // Then
        assertThat(missionCreateResponse.title()).isEqualTo(mission.getTitle());
        verify(missionRepository, times(1)).save(any(Mission.class));
    }

    @Test
    void 미션_단일_조회_성공() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.of(mission));

        // When
        MissionGetOneResponse missionGetOneResponse = missionService.getMission(1L);

        // Then
        assertThat(missionGetOneResponse.title()).isEqualTo(mission.getTitle());
        verify(missionRepository, times(1)).findById(anyLong());
    }

    @Test
    void 오늘의_미션_조회_성공_히스토리가_이미_존재하는_경우() {
        // Given
        LocalDate currentDate = LocalDate.now();
        Mission localMission =
                fixtureMonkey
                        .giveMeBuilder(Mission.class)
                        .set("raisePet", mission.getRaisePet())
                        .build()
                        .sample();
        MissionHistory localMissionHistory =
                MissionHistory.createMissionHistory(
                        localMission, currentDate, localMission.getRaisePet());

        when(missionHistoryRepository.findByAssignedDateAndRaisePet(
                        currentDate, localMission.getRaisePet()))
                .thenReturn(Optional.of(localMissionHistory));
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(member.getRaisePet()).thenReturn(localMission.getRaisePet());

        // When
        MissionGetTodayResponse result = missionService.getOrCreateTodayMission();

        // Then
        assertThat(result).isNotNull();
        assertThat(result.title()).isEqualTo(localMission.getTitle());
        verify(missionHistoryRepository, times(1))
                .findByAssignedDateAndRaisePet(today, localMission.getRaisePet());
        verify(missionHistoryRepository, times(0)).save(any(MissionHistory.class));
        assertEquals(localMission.getRaisePet(), mission.getRaisePet());
    }

    @Test
    void 오늘의_미션_조회_성공_히스토리가_없는_경우() {
        // Given: 초기 설정
        List<Mission> recentMissions = new ArrayList<>();
        recentMissions.add(Mission.builder().title("1일 전 미션").raisePet(RaisePet.DOG).build());
        recentMissions.add(Mission.builder().title("2일 전 미션").raisePet(RaisePet.DOG).build());
        recentMissions.add(Mission.builder().title("3일 전 미션").raisePet(RaisePet.DOG).build());

        List<Mission> availableMissions = new ArrayList<>();
        availableMissions.add(Mission.builder().title("4일 전 미션").raisePet(RaisePet.DOG).build());
        availableMissions.add(Mission.builder().title("5일 전 미션").raisePet(RaisePet.DOG).build());

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(member.getRaisePet()).thenReturn(RaisePet.DOG);

        when(missionRepository.findMissionsAssignedAfterAndByRaisePet(
                        beforeDayByStandard, RaisePet.DOG))
                .thenReturn(recentMissions);

        when(missionRepository.findNotInMissionsAndByRaisePet(recentMissions, RaisePet.DOG))
                .thenReturn(availableMissions);

        when(missionHistoryRepository.save(any(MissionHistory.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When: getOrCreateTodayMission 을 호출하면
        MissionGetTodayResponse result = missionService.getOrCreateTodayMission();

        // Then: 각 메서드들이 실행됐는지 검증
        assertThat(result).isNotNull();
        assertThat(result.title()).isIn("4일 전 미션", "5일 전 미션");
        assertEquals(RaisePet.DOG, member.getRaisePet());
    }

    @Test
    void 오늘의_미션_조회_실패_할당가능한_미션이_없는_경우() {
        // Given: 초기 설정
        List<Mission> emptyMissionList = new ArrayList<>();
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(member.getRaisePet()).thenReturn(mission.getRaisePet());

        when(missionHistoryRepository.findByAssignedDateAndRaisePet(today, mission.getRaisePet()))
                .thenReturn(Optional.empty());

        when(missionRepository.findNotInMissionsAndByRaisePet(
                        emptyMissionList, mission.getRaisePet()))
                .thenReturn(emptyMissionList);

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
        MissionUpdateRequest updateRequest =
                new MissionUpdateRequest("Updated Mission", RaisePet.DOG, "Complete");
        MissionUpdateResponse missionUpdateResponse =
                missionService.updateMission(mission.getId(), updateRequest);

        // Then
        assertThat(missionUpdateResponse.title()).isEqualTo("Updated Mission");
        assertEquals(RaisePet.DOG, mission.getRaisePet()); // RaisePet 검증 추가
        verify(missionRepository, times(1)).findById(anyLong());
        verify(missionRepository, times(1)).save(any(Mission.class));
    }

    @Test
    void 미션_수정_실패_미션이_없는_경우() {
        // Given
        when(missionRepository.findById(anyLong())).thenReturn(Optional.empty());
        MissionUpdateRequest updateRequest =
                new MissionUpdateRequest("Test Mission", RaisePet.DOG, "Complete");

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
