package com.depromeet.stonebed.domain.missionRecord.dto.response;

import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecordStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

public record MissionTabResponse(
        @Schema(description = "기록 ID", example = "1") Long recordId,
        @Schema(description = "이미지 URL", example = "example.jpeg") String imageUrl,
        @Schema(description = "미션 상태", example = "NOT_COMPLETED") MissionRecordStatus status,
        @Schema(description = "미션 제목", example = "산책하기") String missionTitle,
        @Schema(description = "미션 일러스트 이미지", example = "example.jpeg") String illustrationUrl,
        @Schema(description = "기록 내용", example = "오늘 마실다녀왔어요") String content,
        @Schema(description = "완료 일자", example = "2021-10-10") LocalDate completedAt) {

    public static MissionTabResponse of(
            Long recordId,
            String imageUrl,
            MissionRecordStatus status,
            String missionTitle,
            String illustrationUrl,
            String content,
            LocalDate completedAt) {
        return new MissionTabResponse(
                recordId, imageUrl, status, missionTitle, illustrationUrl, content, completedAt);
    }
}
