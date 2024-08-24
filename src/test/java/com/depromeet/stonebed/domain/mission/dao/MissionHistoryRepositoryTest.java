package com.depromeet.stonebed.domain.mission.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.depromeet.stonebed.TestQuerydslConfig;
import com.depromeet.stonebed.domain.member.domain.RaisePet;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionHistory.dao.MissionHistoryRepository;
import com.depromeet.stonebed.domain.missionHistory.domain.MissionHistory;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ActiveProfiles("test")
@ExtendWith(SpringExtension.class)
@DataJpaTest
@Import(TestQuerydslConfig.class)
class MissionHistoryRepositoryTest {

    @Autowired private MissionRepository missionRepository;
    @Autowired private MissionHistoryRepository missionHistoryRepository;

    @Test
    void 미션_히스토리_생성_성공() {
        // Given: 오늘 날짜를 기준으로 미션 히스토리가 있다 (생성할 예정)
        Mission mission = Mission.builder().title("Test Mission").raisePet(RaisePet.DOG).build();
        missionRepository.save(mission);
        LocalDate today = LocalDate.now();

        MissionHistory missionHistory =
                MissionHistory.createMissionHistory(mission, today, RaisePet.DOG);

        // When: 미션 히스토리를 저장하면
        MissionHistory savedMissionHistory = missionHistoryRepository.save(missionHistory);

        // Then: 저장된 미션 히스토리가 올바른지 검증한다
        assertThat(savedMissionHistory.getId()).isNotNull();
        assertThat(savedMissionHistory.getMission().getTitle()).isEqualTo("Test Mission");
        assertThat(savedMissionHistory.getAssignedDate()).isEqualTo(today);
    }

    @Test
    void 미션_히스토리_특정_날짜_조회_성공() {
        // Given: 오늘 날짜를 기준으로 저장된 객체가 있다
        Mission mission = Mission.builder().title("Test Mission").raisePet(RaisePet.CAT).build();
        missionRepository.save(mission);
        LocalDate today = LocalDate.now();

        MissionHistory missionHistory =
                MissionHistory.createMissionHistory(mission, today, RaisePet.CAT);
        missionHistoryRepository.save(missionHistory);

        // When: 특정 날짜(오늘)의 미션 히스토리를 가져오면
        Optional<MissionHistory> foundMissionHistory =
                missionHistoryRepository.findByAssignedDate(today);

        // Then: 가져온 미션 히스토리가 올바른지 검증한다
        assertThat(foundMissionHistory).isPresent();
        assertThat(foundMissionHistory.get().getMission().getTitle()).isEqualTo("Test Mission");
        assertThat(foundMissionHistory.get().getAssignedDate()).isEqualTo(today);
    }
}
