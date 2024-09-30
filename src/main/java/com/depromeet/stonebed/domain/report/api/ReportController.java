package com.depromeet.stonebed.domain.report.api;

import com.depromeet.stonebed.domain.report.application.ReportService;
import com.depromeet.stonebed.domain.report.dto.request.ReportRequest;
import com.depromeet.stonebed.domain.report.dto.response.ReportReasonResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "8. [신고]", description = "신고 기능 관련 API입니다.")
@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {
    private final ReportService reportService;

    @Operation(summary = "신고 사유 목록 조회", description = "신고 사유 목록을 가져옵니다.")
    @GetMapping("/reasons")
    public ResponseEntity<List<ReportReasonResponse>> getReportReasons() {
        List<ReportReasonResponse> reasons = reportService.getReportReasons();
        return ResponseEntity.ok(reasons);
    }

    @Operation(summary = "신고하기", description = "특정 피드를 신고한다.")
    @PostMapping
    public ResponseEntity<Void> reportFeed(@RequestBody ReportRequest reportRequest) {
        reportService.reportFeed(reportRequest);
        return ResponseEntity.ok().build();
    }
}
