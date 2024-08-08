package com.depromeet.stonebed.domain.feed.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record FeedContentGetResponse(
        @Schema(description = "미션 ID", example = "1") Long missionId,
        @Schema(description = "작성자 ID", example = "1") Long authorId,
        @Schema(description = "미션 기록 이미지 URL", example = "example.jpeg")
                String missionRecordImageUrl,
        @Schema(description = "미션 기록 생성일") LocalDate createdDate) {
    public static FeedContentGetResponse from(MissionRecord missionRecord) {
        return new FeedContentGetResponse(
                missionRecord.getId(),
                missionRecord.getMember().getId(),
                missionRecord.getImageUrl(),
                missionRecord.getCreatedAt().toLocalDate());
    }
}
