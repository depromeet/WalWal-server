package com.depromeet.stonebed.domain.report.application;

import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.report.dao.ReportRepository;
import com.depromeet.stonebed.domain.report.domain.Report;
import com.depromeet.stonebed.domain.report.dto.request.ReportRequest;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
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

        Report report =
                Report.createReport(
                        missionRecord, member, reportRequest.reason(), reportRequest.details());

        reportRepository.save(report);
    }
}
