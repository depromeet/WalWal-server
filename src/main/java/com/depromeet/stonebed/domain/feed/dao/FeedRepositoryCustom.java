package com.depromeet.stonebed.domain.feed.dao;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import java.util.List;

public interface FeedRepositoryCustom {
    List<MissionRecord> getFeedContentsUsingCursor(Long missionRecordId, Long memberId, int limit);

    List<MissionRecord> getFeedContents(Long memberId, int limit);
}
