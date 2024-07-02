package com.depromeet.stonebed.domain.record.dao;

import com.depromeet.stonebed.domain.record.domain.Record;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecordRepository extends JpaRepository<Record, Long> {}
