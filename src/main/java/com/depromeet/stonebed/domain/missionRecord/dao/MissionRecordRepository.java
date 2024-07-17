package com.depromeet.stonebed.domain.missionRecord.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MissionRecordRepository extends JpaRepository<MissionRecord, Long> {}
