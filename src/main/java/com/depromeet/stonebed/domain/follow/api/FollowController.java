package com.depromeet.stonebed.domain.follow.api;

import com.depromeet.stonebed.domain.follow.application.FollowService;
import com.depromeet.stonebed.domain.follow.dto.request.FollowCreateRequest;
import com.depromeet.stonebed.domain.follow.dto.response.FollowRelationMemberResponse;
import com.depromeet.stonebed.domain.follow.dto.response.FollowerDeletedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "5. [팔로우]", description = "팔로우 관련 API입니다.")
@RestController
@RequestMapping("/follows")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @PostMapping
    @Operation(summary = "팔로우 추가", description = "팔로우를 추가합니다.")
    public ResponseEntity<Void> followCreate(@Valid @RequestBody FollowCreateRequest request) {
        followService.createFollow(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{targetId}")
    @Operation(summary = "팔로우 취소", description = "팔로우를 취소합니다.")
    public ResponseEntity<FollowerDeletedResponse> followDelete(@PathVariable Long targetId) {
        return ResponseEntity.ok(followService.deleteFollow(targetId));
    }

    @GetMapping("/{targetId}")
    @Operation(summary = "팔로우 상태 확인", description = "팔로우 한 유저와의 팔로우 관계를 조회합니다.")
    public FollowRelationMemberResponse followedUserFindAll(@PathVariable Long targetId) {
        return followService.findFollowRelationByTargetId(targetId);
    }
}
