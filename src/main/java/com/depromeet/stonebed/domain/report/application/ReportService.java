package com.depromeet.stonebed.domain.report.application;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.report.dao.ReportRepository;
import com.depromeet.stonebed.domain.report.domain.Report;
import com.depromeet.stonebed.domain.report.domain.ReportReason;
import com.depromeet.stonebed.domain.report.dto.request.ReportRequest;
import com.depromeet.stonebed.domain.report.dto.response.ReportReasonResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private final ReportRepository reportRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MemberUtil memberUtil;

    public void reportFeed(ReportRequest reportRequest) {
        final Member member = memberUtil.getCurrentMember();

        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(reportRequest.recordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        ReportReason reportReason = ReportReason.fromName(reportRequest.reason());

        Report report =
                Report.builder()
                        .missionRecord(missionRecord)
                        .member(member)
                        .reportReason(reportReason)
                        .details(reportRequest.details())
                        .build();

        reportRepository.save(report);
    }

    public List<ReportReasonResponse> getReportReasons() {
        return Arrays.stream(ReportReason.values())
                .map(reason -> new ReportReasonResponse(reason.name(), reason.getValue()))
                .collect(Collectors.toList());
    }
}
