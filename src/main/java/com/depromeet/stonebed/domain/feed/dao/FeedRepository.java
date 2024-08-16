package com.depromeet.stonebed.domain.feed.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedRepository extends JpaRepository<MissionRecord, Long>, FeedRepositoryCustom {}
