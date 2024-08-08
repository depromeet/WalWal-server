package com.depromeet.stonebed.domain.feed.api;

import com.depromeet.stonebed.domain.feed.application.FeedService;
import com.depromeet.stonebed.domain.feed.dto.request.FeedGetRequest;
import com.depromeet.stonebed.domain.feed.dto.response.FeedGetResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "7. [피드]", description = "피드 관련 API입니다.")
@RestController
@RequestMapping("/feed")
@RequiredArgsConstructor
public class FeedController {
    private final FeedService feedService;

    @GetMapping("/")
    public FeedGetResponse getFeed(
            @Valid @RequestParam(required = false) String cursor,
            @Valid @RequestParam @NotNull @Min(1) int limit) {
        FeedGetRequest request = new FeedGetRequest(cursor, limit);
        return feedService.getFeed(request);
    }
}
