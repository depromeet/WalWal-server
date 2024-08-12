package com.depromeet.stonebed.domain.feed.application;

import com.depromeet.stonebed.domain.feed.dao.FeedRepository;
import com.depromeet.stonebed.domain.feed.dto.request.FeedGetRequest;
import com.depromeet.stonebed.domain.feed.dto.response.FeedContentGetResponse;
import com.depromeet.stonebed.domain.feed.dto.response.FeedGetResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
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

        List<MissionRecord> missionRecords =
                getMissionRecords(request.cursor(), currentMember.getId(), request.limit());

        List<FeedContentGetResponse> feedContentList =
                missionRecords.stream().map(FeedContentGetResponse::from).toList();

        String nextCursor = getNextCursor(missionRecords, request.limit());

        return FeedGetResponse.from(feedContentList, nextCursor);
    }

    private String getNextCursor(List<MissionRecord> records, int limit) {
        if (records.size() < limit) {
            return null;
        }

        MissionRecord lastRecord = records.get(records.size() - 1);
        Long lastId = lastRecord.getId();
        return String.valueOf(lastId);
    }

    private List<MissionRecord> getMissionRecords(String cursor, Long memberId, int limit) {
        if (cursor == null || cursor.isEmpty()) {
            return feedRepository.getFeedContents(memberId, limit);
        }

        return feedRepository.getFeedContentsUsingCursor(Long.parseLong(cursor), memberId, limit);
    }
}
