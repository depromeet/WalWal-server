package com.depromeet.stonebed.domain.missionRecord;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.dao.missionHistory.MissionHistoryRepository;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordSaveRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@ExtendWith(MockitoExtension.class)
public class MissionRecordServiceTest {

    @InjectMocks private MissionRecordService missionRecordService;

    @Mock private MissionRecordRepository missionRecordRepository;
    @Mock private MissionHistoryRepository missionHistoryRepository;
    @Mock private MemberUtil memberUtil;

    private FixtureMonkey fixtureMonkey;

    @BeforeEach
    void setUp() {
        fixtureMonkey =
                FixtureMonkey.builder()
                        .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build();
    }

    @Test
    void 미션기록_성공() {
        // given
        Long missionId = 1L;
        String text = "미션 완료 소감";

        MissionHistory missionHistory = fixtureMonkey.giveMeOne(MissionHistory.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("missionHistory", missionHistory)
                        .set("member", member)
                        .set("status", MissionRecordStatus.COMPLETED)
                        .set("text", text)
                        .sample();

        when(missionHistoryRepository.findLatestOneByMissionId(missionId))
                .thenReturn(Optional.of(missionHistory));
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findByMemberAndMissionHistory(eq(member), eq(missionHistory)))
                .thenReturn(Optional.of(missionRecord)); // 모킹 추가
        when(missionRecordRepository.save(any(MissionRecord.class))).thenReturn(missionRecord);

        MissionRecordSaveRequest request = new MissionRecordSaveRequest(text);

        // when
        missionRecordService.saveMission(missionId, request.text());

        // then
        verify(missionHistoryRepository).findLatestOneByMissionId(missionId);
        verify(memberUtil).getCurrentMember();
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
    void 캘린더미션기록조회_성공() {
        // given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<MissionRecord> missionRecords = fixtureMonkey.giveMe(MissionRecord.class, 5);

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findByMemberIdWithPagination(anyLong(), any(Pageable.class)))
                .thenReturn(missionRecords);

        String cursor = null;
        int limit = 5;

        // when
        MissionRecordCalendarResponse response =
                missionRecordService.getMissionRecordsForCalendar(cursor, limit);

        // then
        then(response).isNotNull();
        then(response.list()).isNotEmpty();

        verify(memberUtil).getCurrentMember();
        verify(missionRecordRepository)
                .findByMemberIdWithPagination(
                        member.getId(),
                        PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "createdAt")));
    }

    @Test
    void 미션참여_성공() {
        // given
        Long missionId = 1L;

        MissionHistory missionHistory = fixtureMonkey.giveMeOne(MissionHistory.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("missionHistory", missionHistory)
                        .set("member", member)
                        .set("status", MissionRecordStatus.IN_PROGRESS)
                        .sample();

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionHistoryRepository.findLatestOneByMissionId(missionId))
                .thenReturn(Optional.of(missionHistory));
        when(missionRecordRepository.findByMemberAndMissionHistory(member, missionHistory))
                .thenReturn(Optional.of(missionRecord));
        when(missionRecordRepository.save(any(MissionRecord.class))).thenReturn(missionRecord);

        // when
        missionRecordService.startMission(missionId);

        // then
        verify(memberUtil).getCurrentMember();
        verify(missionHistoryRepository).findLatestOneByMissionId(missionId);
        verify(missionRecordRepository).findByMemberAndMissionHistory(member, missionHistory);
        verify(missionRecordRepository).save(any(MissionRecord.class));
    }
}
