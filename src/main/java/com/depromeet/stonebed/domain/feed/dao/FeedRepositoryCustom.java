package com.depromeet.stonebed.domain.feed.dao;

import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import java.util.List;

public interface FeedRepositoryCustom {
    List<FindFeedDto> getFeedContentsUsingCursor(Long missionRecordId, Long memberId, int limit);

    FindFeedDto getNextFeedContent(Long missionRecordId, Long memberId);

    FindFeedDto findOneFeedContent(Long recordId);
}
