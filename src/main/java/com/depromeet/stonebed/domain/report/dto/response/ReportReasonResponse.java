package com.depromeet.stonebed.domain.report.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "신고 사유 응답 정보")
public record ReportReasonResponse(
        @Schema(description = "신고 사유 ENUM 값", example = "HARASSMENT") String enumValue,
        @Schema(description = "신고 사유 설명", example = "사기 또는 사칭") String description) {}
