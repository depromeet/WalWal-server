package com.depromeet.stonebed.domain.feed.dao;

import com.depromeet.stonebed.domain.feed.dto.FeedDTO;
import java.util.List;

public interface FeedRepositoryCustom {
    List<FeedDTO> getFeedContentsUsingCursor(Long missionRecordId, Long memberId, int limit);

    List<FeedDTO> getFeedContents(Long memberId, int limit);
}
