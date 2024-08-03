package com.depromeet.stonebed.domain.mission.dao;

import static org.assertj.core.api.Assertions.*;

import com.depromeet.stonebed.TestQuerydslConfig;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestQuerydslConfig.class)
public class MissionRepositoryTest {

    @Autowired private MissionRepository missionRepository;

    @Test
    public void 미션_생성_성공() {
        // Given
        Mission mission = Mission.builder().title("Test Mission").build();

        // When
        Mission savedMission = missionRepository.save(mission);

        // Then
        assertThat(savedMission.getId()).isNotNull();
        assertThat(savedMission.getTitle()).isEqualTo("Test Mission");
    }

    @Test
    public void 고유번호로_미션_조회_성공() {
        // Given
        Mission mission = Mission.builder().title("Test Mission").build();
        missionRepository.save(mission);

        // When
        Mission foundMission = missionRepository.findById(mission.getId()).orElse(null);

        // Then
        assertThat(foundMission).isNotNull();
        assertThat(foundMission.getTitle()).isEqualTo("Test Mission");
    }

    @Test
    public void 미션_삭제_성공() {
        // Given
        Mission mission = Mission.builder().title("Test Mission").build();
        mission = missionRepository.save(mission);

        // When
        missionRepository.deleteById(mission.getId());
        Mission deletedMission = missionRepository.findById(mission.getId()).orElse(null);

        // Then
        assertThat(deletedMission).isNull();
    }
}
