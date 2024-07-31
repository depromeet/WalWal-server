package com.depromeet.stonebed.domain.missionRecord;

import static org.assertj.core.api.BDDAssertions.then;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.image.dao.ImageRepository;
import com.depromeet.stonebed.domain.image.domain.Image;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionStatus;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordSaveRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionCompleteResponse;
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

    @Mock private MissionRepository missionRepository;
    @Mock private MissionRecordRepository missionRecordRepository;
    @Mock private ImageRepository imageRepository;
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
        Long recordId = 1L;
        MissionRecordSaveRequest request = new MissionRecordSaveRequest(missionId, recordId);

        Mission mission = fixtureMonkey.giveMeOne(Mission.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        Image image = fixtureMonkey.giveMeOne(Image.class);
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("mission", mission)
                        .set("member", member)
                        .set("status", MissionStatus.COMPLETED)
                        .sample();

        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(imageRepository.findByTargetId(recordId)).thenReturn(Optional.of(image));
        when(missionRecordRepository.save(any(MissionRecord.class))).thenReturn(missionRecord);

        // when
        missionRecordService.saveMission(request.recordId(), request.missionId());

        // then
        verify(missionRepository).findById(request.missionId());
        verify(memberUtil).getCurrentMember();
        verify(imageRepository).findByTargetId(request.recordId());
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

        Mission mission = fixtureMonkey.giveMeOne(Mission.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("mission", mission)
                        .set("member", member)
                        .set("status", MissionStatus.IN_PROGRESS)
                        .sample();

        when(missionRepository.findById(missionId)).thenReturn(Optional.of(mission));
        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(missionRecordRepository.findByMemberAndMission(member, mission))
                .thenReturn(Optional.of(missionRecord));
        when(missionRecordRepository.save(any(MissionRecord.class))).thenReturn(missionRecord);

        // when
        missionRecordService.startMission(missionId);

        // then
        verify(missionRepository).findById(missionId);
        verify(memberUtil).getCurrentMember();
        verify(missionRecordRepository).findByMemberAndMission(member, mission);
        verify(missionRecordRepository).save(any(MissionRecord.class));
    }

    @Test
    void 미션완료_성공() {
        // given
        Long recordId = 1L;

        Mission mission = fixtureMonkey.giveMeOne(Mission.class);
        Member member = fixtureMonkey.giveMeOne(Member.class);
        MissionRecord missionRecord =
                fixtureMonkey
                        .giveMeBuilder(MissionRecord.class)
                        .set("mission", mission)
                        .set("member", member)
                        .set("status", MissionStatus.COMPLETED)
                        .sample();

        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.of(missionRecord));

        // when
        MissionCompleteResponse response = missionRecordService.getMissionImageUrl(recordId);

        // then
        then(response).isNotNull();
        then(response.missionImageUrl()).isEqualTo(missionRecord.getImageUrl());

        verify(missionRecordRepository).findById(recordId);
    }
}
