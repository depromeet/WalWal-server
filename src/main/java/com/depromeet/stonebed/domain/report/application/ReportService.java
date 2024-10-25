package com.depromeet.stonebed.domain.report.application;

import com.depromeet.stonebed.domain.discord.application.DiscordNotificationService;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.missionRecord.dao.MissionRecordRepository;
import com.depromeet.stonebed.domain.missionRecord.domain.MissionRecord;
import com.depromeet.stonebed.domain.report.dao.ReportRepository;
import com.depromeet.stonebed.domain.report.domain.Report;
import com.depromeet.stonebed.domain.report.domain.ReportDomain;
import com.depromeet.stonebed.domain.report.dto.request.ReportCreateRequest;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReportService {
    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final ReportRepository reportRepository;
    private final MissionRecordRepository missionRecordRepository;
    private final MemberUtil memberUtil;
    private final DiscordNotificationService discordNotificationService;

    public void reportFeed(ReportCreateRequest reportCreateRequest) {
        final Member reporter = memberUtil.getCurrentMember();

        MissionRecord missionRecord =
                missionRecordRepository
                        .findById(reportCreateRequest.recordId())
                        .orElseThrow(() -> new CustomException(ErrorCode.MISSION_RECORD_NOT_FOUND));

        Member reportedMember = missionRecord.getMember();

        Report report =
                Report.createReport(
                        missionRecord.getId(),
                        reporter,
                        ReportDomain.MISSION_RECORD,
                        reportCreateRequest.reason(),
                        reportCreateRequest.details());

        reportRepository.save(report);

        sendReportNotificationToDiscord(
                reporter, reportedMember, missionRecord, reportCreateRequest);
    }

    private void sendReportNotificationToDiscord(
            Member reporter,
            Member reportedMember,
            MissionRecord missionRecord,
            ReportCreateRequest reportCreateRequest) {
        String reportTime = java.time.LocalDateTime.now().format(DATE_TIME_FORMATTER);

        String message =
                String.format(
                        "🚨 **신고 접수 알림** 🚨\n\n"
                                + "**-- 신고자 정보 --**\n"
                                + "**닉네임**: %s\n"
                                + "**신고 시간**: %s\n\n"
                                + "**-- 신고 상세 내용 --**\n"
                                + "**신고 사유**: %s\n"
                                + "**신고 내용**: %s\n\n"
                                + "**-- 신고 대상 정보 --**\n"
                                + "**닉네임**: %s\n"
                                + "**게시글 ID**: %d\n"
                                + "**게시글 이미지 URL**: %s\n"
                                + "**게시글 내용**: %s",
                        reporter.getProfile().getNickname(),
                        reportTime,
                        reportCreateRequest.reason(),
                        reportCreateRequest.details(),
                        reportedMember.getProfile().getNickname(),
                        missionRecord.getId(),
                        missionRecord.getImageUrl() != null
                                ? missionRecord.getImageUrl()
                                : "이미지 없음",
                        missionRecord.getContent() != null ? missionRecord.getContent() : "내용 없음");

        discordNotificationService.sendDiscordMessage(message);
    }
}
