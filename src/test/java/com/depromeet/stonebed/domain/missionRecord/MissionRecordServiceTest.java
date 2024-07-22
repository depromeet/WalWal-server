package com.depromeet.stonebed.domain.missionRecord;

import static org.assertj.core.api.BDDAssertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.domain.mission.dao.MissionRepository;
import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import java.util.Optional;
import net.jqwik.api.Arbitraries;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class MissionRecordServiceTest {

    @InjectMocks private MissionRecordService missionRecordService;

    @Mock private MissionRepository missionRepository;

    @Mock private MissionRecordRepository missionRecordRepository;

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

    // Member Class 생성 후 테스트
    // @Test
    // void completeMission_success() {
    // 	// given
    // 	MissionRecordCreateRequest request =
    // fixtureMonkey.giveMeBuilder(MissionRecordCreateRequest.class)
    // 		.set("missionId", Arbitraries.longs().greaterOrEqual(1L))
    // 		.sample();
    //
    // 	Mission mission = fixtureMonkey.giveMeOne(Mission.class);
    // 	Member member = fixtureMonkey.giveMeOne(Member.class);
    // 	MissionRecord missionRecord = fixtureMonkey.giveMeBuilder(MissionRecord.class)
    // 		.set("mission", mission)
    // 		.set("member", member)
    // 		.set("status", MissionStatus.COMPLETED)
    // 		.sample();
    //
    // 	when(missionRepository.findById(any(Long.class))).thenReturn(Optional.of(mission));
    // 	when(memberUtil.getCurrentMember()).thenReturn(member);
    // 	when(missionRecordRepository.save(any(MissionRecord.class))).thenReturn(missionRecord);
    //
    // 	// when
    // 	MissionRecordCreateResponse response = missionRecordService.completeMission(request);
    //
    // 	// then
    // 	then(response).isNotNull();
    // 	then(response.recordId()).isEqualTo(missionRecord.getId());
    // 	then(response.missionTitle()).isEqualTo(missionRecord.getMission().getTitle());
    //
    // 	verify(missionRepository).findById(request.missionId());
    // 	verify(memberUtil).getCurrentMember();
    // 	verify(missionRecordRepository).save(any(MissionRecord.class));
    // }

    @Test
    void deleteMissionRecord_success() {
        // given
        Long recordId = Arbitraries.longs().greaterOrEqual(1L).sample();
        MissionRecord missionRecord = fixtureMonkey.giveMeOne(MissionRecord.class);

        when(missionRecordRepository.findById(recordId)).thenReturn(Optional.of(missionRecord));

        // when
        missionRecordService.deleteMissionRecord(recordId);

        // then
        verify(missionRecordRepository).findById(recordId);
        verify(missionRecordRepository).delete(missionRecord);
    }

    @Test
    void deleteMissionRecord_notFound() {
        // given
        Long recordId = Arbitraries.longs().greaterOrEqual(1L).sample();

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
}
