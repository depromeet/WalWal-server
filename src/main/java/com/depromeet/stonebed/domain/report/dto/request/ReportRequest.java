package com.depromeet.stonebed.domain.report.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "신고 요청 정보")
public record ReportRequest(
        @Schema(description = "신고할 대상 기록의 ID", example = "123", required = true) @NotNull
                Long recordId,
        @Schema(description = "신고 사유", example = "사기 또는 사칭", required = true) @NotNull
                String reason,
        @Schema(
                        description = "신고 상세 내용 (최대 500자)",
                        example = "해당 게시물은 부적절한 내용을 포함하고 있습니다.",
                        maxLength = 500)
                @Size(max = 500)
                String details) {}
