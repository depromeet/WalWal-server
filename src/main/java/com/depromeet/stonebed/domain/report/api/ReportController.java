package com.depromeet.stonebed.domain.report.api;

import com.depromeet.stonebed.domain.report.application.ReportService;
import com.depromeet.stonebed.domain.report.dto.request.ReportCreateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "피드 신고하기", description = "특정 피드를 신고한다.")
    @PostMapping("/feed")
    public ResponseEntity<Void> reportFeed(@RequestBody ReportCreateRequest reportCreateRequest) {
        reportService.reportFeed(reportCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
