package com.depromeet.stonebed.domain.feed.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.feed.dao.FeedRepository;
import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import com.depromeet.stonebed.domain.feed.dto.request.FeedGetRequest;
import com.depromeet.stonebed.domain.feed.dto.response.FeedGetResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.mission.domain.Mission;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class FeedServiceTest extends FixtureMonkeySetUp {
    int DEFAULT_LIMIT = 5;
    Long DEFAULT_TOTAL_BOOST_COUNT = 100L;
    String DEFAULT_CURSOR = "5";
    String INVALID_CURSOR = "2024-08-01";

    @InjectMocks private FeedService feedService;

    @Mock private FeedRepository feedRepository;

    @Test
    void 피드_조회_성공() {
        // Given
        List<FindFeedDto> feeds = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            feeds.add(
                    FindFeedDto.from(
                            fixtureMonkey.giveMeOne(Mission.class),
                            fixtureMonkey.giveMeOne(MissionRecord.class),
                            fixtureMonkey.giveMeOne(Member.class),
                            DEFAULT_TOTAL_BOOST_COUNT));
        }

        when(feedRepository.getFeedContentsUsingCursor(null, null, DEFAULT_LIMIT))
                .thenReturn(feeds);
        when(feedRepository.getNextFeedContent(
                        feeds.get(feeds.size() - 1).missionRecord().getId(), null))
                .thenReturn(null);

        // When
        FeedGetResponse feedGetResponse =
                feedService.getFeed(new FeedGetRequest(null, null, DEFAULT_LIMIT));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(5);
        assertThat(feedGetResponse.nextCursor()).isEqualTo(null);
    }

    @Test
    void 피드_조회_커서_사용_성공() {
        // Given
        List<FindFeedDto> feeds = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            feeds.add(
                    FindFeedDto.from(
                            fixtureMonkey.giveMeOne(Mission.class),
                            fixtureMonkey.giveMeOne(MissionRecord.class),
                            fixtureMonkey.giveMeOne(Member.class),
                            DEFAULT_TOTAL_BOOST_COUNT));
        }

        when(feedRepository.getFeedContentsUsingCursor(
                        Long.parseLong(DEFAULT_CURSOR), null, DEFAULT_LIMIT))
                .thenReturn(feeds);

        FindFeedDto nextFeed =
                FindFeedDto.from(
                        fixtureMonkey.giveMeOne(Mission.class),
                        fixtureMonkey.giveMeOne(MissionRecord.class),
                        fixtureMonkey.giveMeOne(Member.class),
                        DEFAULT_TOTAL_BOOST_COUNT);

        when(feedRepository.getNextFeedContent(
                        feeds.get(feeds.size() - 1).missionRecord().getId(), null))
                .thenReturn(nextFeed);

        // When
        FeedGetResponse feedGetResponse =
                feedService.getFeed(new FeedGetRequest(DEFAULT_CURSOR, null, DEFAULT_LIMIT));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(5);
        assertThat(feedGetResponse.nextCursor())
                .isEqualTo(String.valueOf(feeds.get(feeds.size() - 1).missionRecord().getId()));
    }

    @Test
    void 피드_조회_커서_사용_마지막_성공() {
        // Given
        List<FindFeedDto> feeds = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            feeds.add(
                    FindFeedDto.from(
                            fixtureMonkey.giveMeOne(Mission.class),
                            fixtureMonkey.giveMeOne(MissionRecord.class),
                            fixtureMonkey.giveMeOne(Member.class),
                            DEFAULT_TOTAL_BOOST_COUNT));
        }

        when(feedRepository.getFeedContentsUsingCursor(
                        Long.parseLong(DEFAULT_CURSOR), null, DEFAULT_LIMIT))
                .thenReturn(feeds);
        when(feedRepository.getNextFeedContent(
                        feeds.get(feeds.size() - 1).missionRecord().getId(), null))
                .thenReturn(null);

        // When
        FeedGetResponse feedGetResponse =
                feedService.getFeed(new FeedGetRequest(DEFAULT_CURSOR, null, DEFAULT_LIMIT));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(3);
        assertThat(feedGetResponse.nextCursor()).isEqualTo(null);
    }

    @Test
    void 피드_조회_유효하지_않은_커서_실패() {
        // Given & When
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () ->
                                feedService.getFeed(
                                        new FeedGetRequest(INVALID_CURSOR, null, DEFAULT_LIMIT)));

        // Then: 에러코드 검증
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT);
    }
}
