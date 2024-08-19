package com.depromeet.stonebed.domain.feed.application;

import com.depromeet.stonebed.domain.feed.dao.FeedRepository;
import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import com.depromeet.stonebed.domain.feed.dto.request.FeedGetRequest;
import com.depromeet.stonebed.domain.feed.dto.response.FeedContentGetResponse;
import com.depromeet.stonebed.domain.feed.dto.response.FeedGetResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FeedService {
    private final FeedRepository feedRepository;
    private final MemberUtil memberUtil;

    @Transactional(readOnly = true)
    public FeedGetResponse getFeed(FeedGetRequest request) {
        Member currentMember = memberUtil.getCurrentMember();

        List<FindFeedDto> feeds =
                getFeeds(request.cursor(), currentMember.getId(), request.limit());

        List<FeedContentGetResponse> feedContentList =
                feeds.stream().map(FeedContentGetResponse::from).toList();

        String nextCursor = getNextCursor(feeds, request.limit());

        return FeedGetResponse.from(feedContentList, nextCursor);
    }

    private String getNextCursor(List<FindFeedDto> records, int limit) {
        if (records.size() < limit) {
            return null;
        }

        FindFeedDto lastRecord = records.get(records.size() - 1);
        Long lastId = lastRecord.missionRecord().getId();
        return String.valueOf(lastId);
    }

    private List<FindFeedDto> getFeeds(String cursor, Long memberId, int limit) {
        if (cursor == null || cursor.isEmpty()) {
            return feedRepository.getFeedContents(memberId, limit);
        }

        return feedRepository.getFeedContentsUsingCursor(parseCursor(cursor), memberId, limit);
    }

    private Long parseCursor(String cursor) {
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            throw new CustomException(ErrorCode.INVALID_CURSOR_FORMAT);
        }
    }
}
