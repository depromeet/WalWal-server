package com.depromeet.stonebed.domain.feed.dto;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;

public record FeedDTO(
        Mission mission, MissionRecord missionRecord, Member author, Long totalBoostCount) {}
