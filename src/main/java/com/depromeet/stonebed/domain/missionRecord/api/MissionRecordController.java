package com.depromeet.stonebed.domain.missionRecord.api;

import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCalendarRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "4. [미션 기록]", description = "미션 기록 관련 API입니다.")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class MissionRecordController {

    private final MissionRecordService missionRecordService;

    @Operation(summary = "미션 탭 상태 조회", description = "미션 탭의 상태를 조회한다.")
    @GetMapping("/{missionId}/status")
    public MissionTabResponse getMissionTabStatus(@PathVariable("missionId") Long missionId) {
        return missionRecordService.getMissionTabStatus(missionId);
    }

    @Operation(summary = "미션 참여", description = "미션 참여하기.")
    @PostMapping("/{missionId}/start")
    public ResponseEntity<Void> startMission(@PathVariable("missionId") Long missionId) {
        missionRecordService.startMission(missionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "미션 기록 저장", description = "미션 완료 후 기록을 저장한다.")
    @PostMapping
    public ResponseEntity<Void> saveMission(@PathVariable("missionId") Long missionId) {
        missionRecordService.saveMission(missionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "미션 기록 삭제", description = "미션 기록을 삭제한다.")
    @DeleteMapping("/{recordId}")
    public void deleteMissionRecord(@PathVariable Long recordId) {
        missionRecordService.deleteMissionRecord(recordId);
    }

    @Operation(summary = "캘린더 형식의 미션 기록 조회", description = "회원의 미션 기록을 페이징하여 조회한다.")
    @PostMapping("/calendar")
    public MissionRecordCalendarResponse getMissionRecordsForCalendar(
            @Valid @RequestBody MissionRecordCalendarRequest request) {
        return missionRecordService.getMissionRecordsForCalendar(request.cursor(), request.limit());
    }

    @Operation(summary = "수행한 총 미션 기록 수", description = "회원이 수행한 총 미션 기록 수를 조회한다.")
    @PostMapping("/complete/total")
    public Long getTotalMissionRecords() {
        return missionRecordService.getTotalMissionRecords();
    }
}
