package com.depromeet.stonebed.domain.feed.dto;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;

public record FindFeedDto(
        Mission mission,
        MissionRecord missionRecord,
        Member author,
        Long totalCommentCount,
        Long totalBoostCount) {
    public static FindFeedDto from(
            Mission mission,
            MissionRecord missionRecord,
            Member author,
            Long totalCommentCount,
            Long totalBoostCount) {
        return new FindFeedDto(mission, missionRecord, author, totalCommentCount, totalBoostCount);
    }
}
