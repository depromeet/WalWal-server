package com.depromeet.stonebed.domain.feed.application;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.verify;

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
import com.depromeet.stonebed.global.util.MemberUtil;
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

    @InjectMocks private FeedService feedService;

    @Mock private FeedRepository feedRepository;
    @Mock private MemberUtil memberUtil;

    @Test
    void 피드_조회_성공() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<FindFeedDto> feeds = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            feeds.add(
                    FindFeedDto.from(
                            fixtureMonkey.giveMeOne(Mission.class),
                            fixtureMonkey.giveMeOne(MissionRecord.class),
                            fixtureMonkey.giveMeOne(Member.class),
                            100L));
        }

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(feedRepository.getFeedContents(member.getId(), 5)).thenReturn(feeds);

        // When
        FeedGetResponse feedGetResponse = feedService.getFeed(new FeedGetRequest(null, 5));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(5);
        String nextCursor = feeds.get(feeds.size() - 1).missionRecord().getId().toString();
        assertThat(feedGetResponse.nextCursor()).isEqualTo(nextCursor);
        verify(memberUtil).getCurrentMember();
        verify(feedRepository).getFeedContents(member.getId(), 5);
    }

    @Test
    void 피드_조회_커서_사용_성공() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<FindFeedDto> feeds = new ArrayList<>();
        String cursor = "5";

        for (int i = 0; i < 5; i++) {
            feeds.add(
                    FindFeedDto.from(
                            fixtureMonkey.giveMeOne(Mission.class),
                            fixtureMonkey.giveMeOne(MissionRecord.class),
                            fixtureMonkey.giveMeOne(Member.class),
                            100L));
        }

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(feedRepository.getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5))
                .thenReturn(feeds);

        // When
        FeedGetResponse feedGetResponse = feedService.getFeed(new FeedGetRequest(cursor, 5));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(5);
        String nextCursor = feeds.get(feeds.size() - 1).missionRecord().getId().toString();
        assertThat(feedGetResponse.nextCursor()).isEqualTo(nextCursor);
        verify(memberUtil).getCurrentMember();
        verify(feedRepository)
                .getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5);
    }

    @Test
    void 피드_조회_커서_사용_마지막_성공() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<FindFeedDto> feeds = new ArrayList<>();
        String cursor = "5";

        for (int i = 0; i < 3; i++) {
            feeds.add(
                    FindFeedDto.from(
                            fixtureMonkey.giveMeOne(Mission.class),
                            fixtureMonkey.giveMeOne(MissionRecord.class),
                            fixtureMonkey.giveMeOne(Member.class),
                            100L));
        }

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(feedRepository.getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5))
                .thenReturn(feeds);

        // When
        FeedGetResponse feedGetResponse = feedService.getFeed(new FeedGetRequest(cursor, 5));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(3);
        assertThat(feedGetResponse.nextCursor()).isEqualTo(null);
        verify(memberUtil).getCurrentMember();
        verify(feedRepository)
                .getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5);
    }

    @Test
    void 피드_조회_유효하지_않은_커서_실패() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        String cursor = "2024-08-01";

        when(memberUtil.getCurrentMember()).thenReturn(member);

        // When
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> feedService.getFeed(new FeedGetRequest(cursor, 5)));

        // Then: 에러코드 검증
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.INVALID_CURSOR_FORMAT);
    }
}
