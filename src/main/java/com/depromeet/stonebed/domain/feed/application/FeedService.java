package com.depromeet.stonebed.domain.feed.application;

import com.depromeet.stonebed.domain.feed.dao.FeedRepository;
import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import com.depromeet.stonebed.domain.feed.dto.request.FeedGetRequest;
import com.depromeet.stonebed.domain.feed.dto.response.FeedContentGetResponse;
import com.depromeet.stonebed.domain.feed.dto.response.FeedGetResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;

    @Transactional(readOnly = true)
    public FeedGetResponse getFeed(FeedGetRequest request) {
        List<FindFeedDto> feeds = getFeeds(request.cursor(), request.memberId(), request.limit());

        List<FeedContentGetResponse> feedContentList =
                feeds.stream().map(FeedContentGetResponse::from).toList();

        String nextCursor = getNextCursor(feeds, request.limit());

        if (nextFeedNotExists(feeds, request.memberId())) {
            nextCursor = null;
        }

        return FeedGetResponse.from(feedContentList, nextCursor);
    }

    private boolean nextFeedNotExists(List<FindFeedDto> feeds, Long memberId) {
        Long lastFeedId = getLastId(feeds);
        if (lastFeedId == null) {
            return true;
        }

        return feedRepository.getNextFeedContent(lastFeedId, memberId) == null;
    }

    private String getNextCursor(List<FindFeedDto> records, int limit) {
        if (records.size() <= limit) {
            return null;
        }

        return String.valueOf(getLastId(records));
    }

    private Long getLastId(List<FindFeedDto> records) {
        if (records.isEmpty()) {
            return null;
        }

        return records.get(records.size() - 1).missionRecord().getId();
    }

    private List<FindFeedDto> getFeeds(String cursor, Long memberId, int limit) {
        return feedRepository.getFeedContentsUsingCursor(parseCursor(cursor), memberId, limit);
    }

    private Long parseCursor(String cursor) {
        if (cursor == null) {
            return null;
        }

        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR_FORMAT);
        }
    }
}
