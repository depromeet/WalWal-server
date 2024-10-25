package com.depromeet.stonebed.domain.feed.api;

import com.depromeet.stonebed.domain.feed.application.FeedService;
import com.depromeet.stonebed.domain.feed.dto.request.FeedGetRequest;
import com.depromeet.stonebed.domain.feed.dto.response.v1.FeedContentGetResponse;
import com.depromeet.stonebed.domain.feed.dto.response.v1.FeedGetResponse;
import com.depromeet.stonebed.domain.feed.dto.response.v2.FeedGetResponseV2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. [피드]", description = "피드 관련 API입니다.")
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @Operation(summary = "피드 조회", description = "내 피드를 조회하는 API입니다.")
    @GetMapping
    public FeedGetResponse feedFind(@Valid FeedGetRequest request) {
        return feedService.findFeed(request);
    }

    @Operation(summary = "피드 조회", description = "내 피드를 조회하는 API입니다.")
    @GetMapping("/v2")
    public FeedGetResponseV2 feedFindV2(@Valid FeedGetRequest request) {
        return feedService.findFeedV2(request);
    }

    @Operation(summary = "단일 피드 조회", description = "단일 피드를 조회하는 API입니다.")
    @GetMapping("/{recordId}")
    public FeedContentGetResponse feedFindOne(@PathVariable Long recordId) {
        return feedService.findFeedOne(recordId);
    }
}
