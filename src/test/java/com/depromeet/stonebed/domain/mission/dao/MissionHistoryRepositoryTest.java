package com.depromeet.stonebed.domain.mission.dao;

import static org.assertj.core.api.Assertions.assertThat;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.mission.domain.MissionHistory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@DataJpaTest
public class MissionHistoryRepositoryTest {

    @Autowired private MissionRepository missionRepository;
    @Autowired private MissionHistoryRepository missionHistoryRepository;

    @Test
    public void 미션_히스토리_생성_성공() {
        // Given: 오늘 날짜를 기준으로 미션 히스토리가 있다 (생성할 예정)
        Mission mission = Mission.builder().title("Test Mission").build();
        missionRepository.save(mission);
        LocalDate today = LocalDate.now();

        MissionHistory missionHistory =
                MissionHistory.builder().mission(mission).assignedDate(today).build();

        // When: 미션 히스토리를 저장하면
        MissionHistory savedMissionHistory = missionHistoryRepository.save(missionHistory);

        // Then: 저장된 미션 히스토리가 올바른지 검증한다
        assertThat(savedMissionHistory.getId()).isNotNull();
        assertThat(savedMissionHistory.getMission().getTitle()).isEqualTo("Test Mission");
        assertThat(savedMissionHistory.getAssignedDate()).isEqualTo(today);
    }

    @Test
    public void 미션_히스토리_특정_날짜_조회_성공() {
        // Given: 오늘 날짜를 기준으로 저장된 객체가 있다
        Mission mission = Mission.builder().title("Test Mission").build();
        missionRepository.save(mission);
        LocalDate today = LocalDate.now();

        MissionHistory missionHistory =
                MissionHistory.builder().mission(mission).assignedDate(today).build();
        missionHistoryRepository.save(missionHistory);

        // When: 특정 날짜(오늘)의 미션 히스토리를 가져오면
        Optional<MissionHistory> foundMissionHistory =
                missionHistoryRepository.findByAssignedDate(today);

        // Then: 가져온 미션 히스토리가 올바른지 검증한다
        assertThat(foundMissionHistory).isPresent();
        assertThat(foundMissionHistory.get().getMission().getTitle()).isEqualTo("Test Mission");
        assertThat(foundMissionHistory.get().getAssignedDate()).isEqualTo(today);
    }

    @Test
    public void 미션_히스토리_리스트_5일전_조회_성공() {
        // Given: 최근 10일간 미션과 미션 히스토리를 미리 만들어둔다
        Mission mission = Mission.builder().title("Test Mission").build();
        missionRepository.save(mission);

        LocalDate today = LocalDate.now();
        for (int i = 1; i < 11; i++) {
            LocalDate date = today.minusDays(i);
            MissionHistory missionHistory =
                    MissionHistory.builder().mission(mission).assignedDate(date).build();
            missionHistoryRepository.save(missionHistory);
        }

        LocalDate fiveDaysAgo = today.minusDays(5);

        // When: 5일 전 히스토리 기록을 가져오면
        List<MissionHistory> missionHistories =
                missionHistoryRepository.findByAssignedDateBefore(fiveDaysAgo);

        // Then: 실제로 5일 전의 데이터가 가져와졌는지 확인한다
        assertThat(missionHistories).isNotEmpty();
        assertThat(missionHistories.size()).isEqualTo(5);
        assertThat(missionHistories)
                .allMatch(history -> history.getAssignedDate().isBefore(fiveDaysAgo));
    }
}
