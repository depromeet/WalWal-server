package com.depromeet.stonebed.domain.mission.dao;

import com.depromeet.stonebed.domain.mission.domain.Mission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRepository extends JpaRepository<Mission, Long>, MissionRepositoryCustom {}
