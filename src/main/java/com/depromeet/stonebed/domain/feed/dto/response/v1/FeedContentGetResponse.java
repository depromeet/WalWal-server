package com.depromeet.stonebed.domain.feed.dto.response.v1;

import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record FeedContentGetResponse(
        @Schema(description = "미션 ID", example = "1") Long missionId,
        @Schema(description = "미션 제목", example = "산책하기") String missionTitle,
        @Schema(description = "미션 완료 메시지", example = "산책하기 미션을 수행했어요!")
                String missionCompleteMessage,
        @Schema(description = "미션 기록 ID", example = "1") Long missionRecordId,
        @Schema(description = "작성자 ID", example = "1") Long authorId,
        @Schema(description = "작성자 프로필 닉네임") String authorProfileNickname,
        @Schema(description = "작성자 프로필 이미지 URL") String authorProfileImageUrl,
        @Schema(description = "미션 기록 이미지 URL", example = "example.jpeg")
                String missionRecordImageUrl,
        @Schema(description = "미션 기록 생성일") LocalDate createdDate,
        @Schema(description = "부스트") Long totalBoostCount,
        @Schema(description = "댓글 수", example = "12") Integer totalCommentCount,
        @Schema(description = "미션 기록 컨텐츠") String content) {
    public static FeedContentGetResponse from(FindFeedDto missionRecord) {
        return new FeedContentGetResponse(
                missionRecord.mission().getId(),
                missionRecord.mission().getTitle(),
                missionRecord.mission().getCompleteMessage(),
                missionRecord.missionRecord().getId(),
                missionRecord.author().getId(),
                missionRecord.author().getProfile().getNickname(),
                missionRecord.author().getProfile().getProfileImageUrl(),
                missionRecord.missionRecord().getImageUrl(),
                missionRecord.missionRecord().getCreatedAt().toLocalDate(),
                missionRecord.totalBoostCount(),
                missionRecord.totalCommentCount(),
                missionRecord.missionRecord().getContent());
    }
}
