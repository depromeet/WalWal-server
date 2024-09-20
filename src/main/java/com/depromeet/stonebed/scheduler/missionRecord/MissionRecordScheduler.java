package com.depromeet.stonebed.scheduler.missionRecord;

import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionRecordScheduler {
    private final MissionRecordService missionRecordService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateMissionStatus() {
        missionRecordService.expiredMissionsToNotCompletedUpdate();
    }
}
