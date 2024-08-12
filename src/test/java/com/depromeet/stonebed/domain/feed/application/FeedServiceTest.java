package com.depromeet.stonebed.domain.feed.application;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.FixtureMonkeySetUp;
import com.depromeet.stonebed.domain.feed.dao.FeedRepository;
import com.depromeet.stonebed.domain.feed.dto.request.FeedGetRequest;
import com.depromeet.stonebed.domain.feed.dto.response.FeedGetResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
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
        List<MissionRecord> missionRecords = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            missionRecords.add(fixtureMonkey.giveMeOne(MissionRecord.class));
        }

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(feedRepository.getFeedContents(member.getId(), 5)).thenReturn(missionRecords);

        // When
        FeedGetResponse feedGetResponse = feedService.getFeed(new FeedGetRequest(null, 5));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(5);
        String nextCursor = missionRecords.get(missionRecords.size() - 1).getId().toString();
        assertThat(feedGetResponse.nextCursor()).isEqualTo(nextCursor);
        verify(memberUtil).getCurrentMember();
        verify(feedRepository).getFeedContents(member.getId(), 5);
    }

    @Test
    void 피드_조회_커서_사용_성공() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<MissionRecord> missionRecords = new ArrayList<>();
        String cursor = "5";

        for (int i = 0; i < 5; i++) {
            missionRecords.add(fixtureMonkey.giveMeOne(MissionRecord.class));
        }

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(feedRepository.getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5))
                .thenReturn(missionRecords);

        // When
        FeedGetResponse feedGetResponse = feedService.getFeed(new FeedGetRequest(cursor, 5));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(5);
        String nextCursor = missionRecords.get(missionRecords.size() - 1).getId().toString();
        assertThat(feedGetResponse.nextCursor()).isEqualTo(nextCursor);
        verify(memberUtil).getCurrentMember();
        verify(feedRepository)
                .getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5);
    }

    @Test
    void 피드_조회_커서_사용_마지막_성공() {
        // Given
        Member member = fixtureMonkey.giveMeOne(Member.class);
        List<MissionRecord> missionRecords = new ArrayList<>();
        String cursor = "5";

        for (int i = 0; i < 3; i++) {
            missionRecords.add(fixtureMonkey.giveMeOne(MissionRecord.class));
        }

        when(memberUtil.getCurrentMember()).thenReturn(member);
        when(feedRepository.getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5))
                .thenReturn(missionRecords);

        // When
        FeedGetResponse feedGetResponse = feedService.getFeed(new FeedGetRequest(cursor, 5));

        // Then
        assertThat(feedGetResponse.list().size()).isEqualTo(3);
        assertThat(feedGetResponse.nextCursor()).isEqualTo(null);
        verify(memberUtil).getCurrentMember();
        verify(feedRepository)
                .getFeedContentsUsingCursor(Long.parseLong(cursor), member.getId(), 5);
    }
}
