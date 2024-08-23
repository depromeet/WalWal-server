package com.depromeet.stonebed.domain.missionRecord.application;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MissionRecordScheduledService {
    private final MissionRecordService missionRecordService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateMissionStatus() {
        missionRecordService.expiredMissionsToNotCompletedUpdate();
    }
}
