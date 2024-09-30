package com.depromeet.stonebed.domain.report.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReportRequest(
        @NotNull Long recordId, @NotNull String reason, @Size(max = 500) String details) {}
