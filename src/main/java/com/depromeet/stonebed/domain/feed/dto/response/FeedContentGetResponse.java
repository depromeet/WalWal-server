package com.depromeet.stonebed.domain.feed.dto.response;

import com.depromeet.stonebed.domain.feed.dto.FindFeedDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record FeedContentGetResponse(
        @Schema(description = "미션 ID", example = "1") Long missionId,
        @Schema(description = "미션 기록 ID", example = "1") Long missionRecordId,
        @Schema(description = "작성자 ID", example = "1") Long authorId,
        @Schema(description = "미션 기록 이미지 URL", example = "example.jpeg")
                String missionRecordImageUrl,
        @Schema(description = "미션 기록 생성일") LocalDate createdDate,
        @Schema(description = "부스트") Long totalBoostCount,
        @Schema(description = "미션 기록 컨텐츠") String content) {
    public static FeedContentGetResponse from(FindFeedDto missionRecord) {
        return new FeedContentGetResponse(
                missionRecord.mission().getId(),
                missionRecord.missionRecord().getId(),
                missionRecord.author().getId(),
                missionRecord.missionRecord().getImageUrl(),
                missionRecord.missionRecord().getCreatedAt().toLocalDate(),
                missionRecord.totalBoostCount(),
                missionRecord.missionRecord().getContent());
    }
}
