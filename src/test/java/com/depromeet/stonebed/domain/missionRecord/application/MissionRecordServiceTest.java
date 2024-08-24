package com.depromeet.stonebed.domain.missionRecord.application;

import static org.assertj.core.api.BDDAssertions.then;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionHistory.dao.MissionHistoryRepository;
import com.depromeet.stonebed.domain.missionHistory.domain.MissionHistory;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordBoostRepository;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordBoost;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCalendarRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordSaveRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCompleteTotal;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class MissionRecordServiceTest extends FixtureMonkeySetUp {

    @InjectMocks private MissionRecordService missionRecordService;
    @Mock private FcmNotificationService fcmNotificationService;
    @Mock private MissionRecordRepository missionRecordRepository;
    @Mock private MissionRepository missionRepository;
    @Mock private MissionHistoryRepository missionHistoryRepository;
    @Mock private MissionRecordBoostRepository missionRecordBoostRepository;
    @Mock private MemberUtil memberUtil;

    @Test
    void 미션기록_성공() {
        // given
        String content = "미션 완료 소감";
        RaisePet raisePet = RaisePet.DOG;

        Mission mission = fixtureMonkey.giveMeOne(Mission.class);
        MissionHistory missionHistory = fixtureMonkey.giveMeOne(MissionHistory.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("missionHistory", missionHistory)
                        .set("member", member)
                        .set("status", MissionRecordStatus.COMPLETED)
                        .set("content", content)
                        .sample();

        when(missionHistoryRepository.findLatestOneByMissionIdRaisePet(mission.getId(), raisePet))
                .thenReturn(Optional.of(missionHistory));
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRepository.findById(mission.getId())).thenReturn(Optional.of(mission));
        when(missionRecordRepository.findByMemberAndMissionHistory(eq(member), eq(missionHistory)))
                .thenReturn(Optional.of(missionRecord)); // 모킹 추가
        when(missionRecordRepository.save(any(MissionRecord.class))).thenReturn(missionRecord);

        MissionRecordSaveRequest request = MissionRecordSaveRequest.of(mission.getId(), content);

        // when
        missionRecordService.saveMission(mission.getId(), request.content());

        // then
        verify(missionHistoryRepository)
                .findLatestOneByMissionIdRaisePet(mission.getId(), raisePet);
        verify(memberUtil).getCurrentMember();
        verify(missionRepository).findById(mission.getId());
        verify(missionRecordRepository)
                .findByMemberAndMissionHistory(eq(member), eq(missionHistory));
        verify(missionRecordRepository).save(any(MissionRecord.class));
    }

    @Test
    void 미션기록삭제_성공() {
        // given
        Long recordId = 1L;
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);

        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.of(missionRecord));

        // when
        missionRecordService.deleteMissionRecord(recordId);

        // then
        verify(missionRecordRepository).findById(recordId);
        verify(missionRecordRepository).delete(missionRecord);
    }

    @Test
    void 미션기록삭제_실패() {
        // given
        Long recordId = 1L;

        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        // when & then
        CustomException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        CustomException.class,
                        () -> missionRecordService.deleteMissionRecord(recordId));

        then(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_RECORD_NOT_FOUND);

        verify(missionRecordRepository).findById(recordId);
        verify(missionRecordRepository, never()).delete(any(MissionRecord.class));
    }

    @Test
    void 본인의_미션_기록_캘린더를_조회합니다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<MissionRecord> missionRecords = fixtureMonkey.giveMe(MissionRecord.class, 5);

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findByMemberIdWithPagination(anyLong(), any(Pageable.class)))
                .thenReturn(missionRecords);

        String cursor = null;
        int limit = 5;

        MissionRecordCalendarRequest request =
                new MissionRecordCalendarRequest(cursor, limit, null);

        // when
        MissionRecordCalendarResponse response =
                missionRecordService.getMissionRecordsForCalendar(
                        request); // Pass null for memberId

        // then
        then(response).isNotNull();
        then(response.list()).isNotEmpty();

        verify(memberUtil).getCurrentMember(); // This will now be invoked
        verify(missionRecordRepository)
                .findByMemberIdWithPagination(
                        member.getId(),
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt")));
    }

    @Test
    void 다른_사용자의_미션_기록_캘린더를_조회합니다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<MissionRecord> missionRecords = fixtureMonkey.giveMe(MissionRecord.class, 5);

        when(missionRecordRepository.findByMemberIdWithPagination(anyLong(), any(Pageable.class)))
                .thenReturn(missionRecords);

        String cursor = null;
        int limit = 5;

        MissionRecordCalendarRequest request =
                new MissionRecordCalendarRequest(cursor, limit, member.getId());

        // when
        MissionRecordCalendarResponse response =
                missionRecordService.getMissionRecordsForCalendar(request);

        // then
        then(response).isNotNull();
        then(response.list()).isNotEmpty();

        verify(missionRecordRepository)
                .findByMemberIdWithPagination(
                        member.getId(),
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt")));
    }

    @Test
    void 미션참여_성공() {
        // given
        Long missionId = 1L;

        MissionHistory missionHistory =
                fixtureMonkey
                        .giveMeBuilder(MissionHistory.class)
                        .set("raisePet", RaisePet.DOG)
                        .sample();
        Member member =
                fixtureMonkey.giveMeBuilder(Member.class).set("raisePet", RaisePet.DOG).sample();
        ;
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("missionHistory", missionHistory)
                        .set("member", member)
                        .set("status", MissionRecordStatus.IN_PROGRESS)
                        .sample();
        Mission mission =
                fixtureMonkey
                        .giveMeBuilder(Mission.class)
                        .set("id", missionId)
                        .set("raisePet", RaisePet.DOG)
                        .sample();

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionHistoryRepository.findLatestOneByMissionIdRaisePet(
                        missionId, mission.getRaisePet()))
                .thenReturn(Optional.of(missionHistory));
        when(missionRecordRepository.findByMemberAndMissionHistory(member, missionHistory))
                .thenReturn(Optional.of(missionRecord));
        when(missionRecordRepository.save(any(MissionRecord.class))).thenReturn(missionRecord);

        // when
        missionRecordService.startMission(missionId);

        // then
        verify(memberUtil).getCurrentMember();
        verify(missionRepository).findById(missionId);
        verify(missionHistoryRepository)
                .findLatestOneByMissionIdRaisePet(missionId, mission.getRaisePet());
        verify(missionRecordRepository).findByMemberAndMissionHistory(member, missionHistory);
        verify(missionRecordRepository).save(any(MissionRecord.class));
    }

    @Test
    void 완료한_미션_수를_조회합니다() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);

        // Create a list of mission records with 3 COMPLETED statuses
        List<MissionRecord> missionRecords =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("member", member)
                        .set("status", MissionRecordStatus.COMPLETED)
                        .sampleList(3);

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.countByMemberIdAndStatus(
                        member.getId(), MissionRecordStatus.COMPLETED))
                .thenReturn((long) missionRecords.size());

        // when
        MissionRecordCompleteTotal completedMissionCount =
                missionRecordService.getTotalMissionRecords(member.getId());

        // then
        then(completedMissionCount.totalCount()).isEqualTo(3);

        verify(memberUtil).getCurrentMember();
        verify(missionRecordRepository)
                .countByMemberIdAndStatus(member.getId(), MissionRecordStatus.COMPLETED);
    }

    @Test
    void 부스트_성공() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findById(missionRecord.getId()))
                .thenReturn(Optional.of(missionRecord));
        when(missionRecordBoostRepository.save(any(MissionRecordBoost.class))).thenReturn(null);

        // When
        missionRecordService.createBoost(missionRecord.getId(), 10L);

        // Then
        verify(memberUtil).getCurrentMember();
        verify(missionRecordRepository).findById(missionRecord.getId());
        verify(missionRecordBoostRepository).save(any(MissionRecordBoost.class));
    }

    @Test
    void 미션탭_상태_미션_완료() {
        // Given
        Long missionId = 1L;
        Member member = fixtureMonkey.giveMeOne(Member.class);
        Mission mission = fixtureMonkey.giveMeBuilder(Mission.class).set("id", missionId).sample();
        MissionHistory missionHistory =
                fixtureMonkey.giveMeBuilder(MissionHistory.class).set("mission", mission).sample();
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("member", member)
                        .set("missionHistory", missionHistory)
                        .set("status", MissionRecordStatus.COMPLETED)
                        .sample();

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionHistoryRepository.findLatestOneByMissionIdRaisePet(
                        missionId, mission.getRaisePet()))
                .thenReturn(Optional.of(missionHistory));
        when(missionRecordRepository.findByMemberAndMissionHistory(member, missionHistory))
                .thenReturn(Optional.of(missionRecord));

        // When
        MissionTabResponse response = missionRecordService.getMissionTabStatus(missionId);

        // Then
        then(response).isNotNull();
        then(response.imageUrl()).isEqualTo(missionRecord.getImageUrl());
        then(response.status()).isEqualTo(MissionRecordStatus.COMPLETED);

        verify(memberUtil).getCurrentMember();
        verify(missionRepository).findById(missionId);
        verify(missionHistoryRepository)
                .findLatestOneByMissionIdRaisePet(missionId, mission.getRaisePet());
        verify(missionRecordRepository).findByMemberAndMissionHistory(member, missionHistory);
    }

    @Test
    void 미션탭_상태_미션_진행중() {
        // Given
        Long missionId = 1L;
        Member member = fixtureMonkey.giveMeOne(Member.class);
        Mission mission = fixtureMonkey.giveMeBuilder(Mission.class).set("id", missionId).sample();
        MissionHistory missionHistory =
                fixtureMonkey.giveMeBuilder(MissionHistory.class).set("mission", mission).sample();

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(missionHistoryRepository.findLatestOneByMissionIdRaisePet(
                        missionId, mission.getRaisePet()))
                .thenReturn(Optional.of(missionHistory));
        when(missionRecordRepository.findByMemberAndMissionHistory(member, missionHistory))
                .thenReturn(Optional.empty());

        // When
        MissionTabResponse response = missionRecordService.getMissionTabStatus(missionId);

        // Then
        then(response).isNotNull();
        then(response.imageUrl()).isNull();
        then(response.status()).isEqualTo(MissionRecordStatus.NOT_COMPLETED);

        verify(memberUtil).getCurrentMember();
        verify(missionRepository).findById(missionId);
        verify(missionHistoryRepository)
                .findLatestOneByMissionIdRaisePet(missionId, mission.getRaisePet());
        verify(missionRecordRepository).findByMemberAndMissionHistory(member, missionHistory);
    }

    @Test
    void 미션탭_상태_미션_시작안함() {
        // Given
        Long missionId = 1L;

        when(missionRepository.findById(missionId)).thenReturn(Optional.empty());

        // When & Then
        CustomException exception =
                org.junit.jupiter.api.Assertions.assertThrows(
                        CustomException.class,
                        () -> missionRecordService.getMissionTabStatus(missionId));

        then(exception.getErrorCode()).isEqualTo(ErrorCode.MISSION_NOT_FOUND);

        verify(missionRepository).findById(missionId);
        verify(missionHistoryRepository, never())
                .findLatestOneByMissionIdRaisePet(anyLong(), any());
        verify(missionRecordRepository, never()).findByMemberAndMissionHistory(any(), any());
    }

    @Test
    void 부스트_실패_내_피드인_경우() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("member", member)
                        .build()
                        .sample();

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findById(missionRecord.getId()))
                .thenReturn(Optional.of(missionRecord));

        // When
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> missionRecordService.createBoost(missionRecord.getId(), 10L));

        // Then
        assertEquals(ErrorCode.BOOST_UNAVAILABLE_MY_FEED, exception.getErrorCode());
    }
}
