package com.depromeet.stonebed.domain.missionRecord.api;

import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordBoostRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCalendarRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordSaveRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordStartRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCompleteTotal;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordIdResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordTabListResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "4. [미션 기록]", description = "미션 기록 관련 API입니다.")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class MissionRecordController {

    private final MissionRecordService missionRecordService;

    @Operation(summary = "미션 탭 완료된 기록 리스트", description = "미션 탭에서 완료된 기록 리스트를 조회한다.")
    @GetMapping
    public MissionRecordTabListResponse missionRecordsFind(@RequestParam Long missionId) {
        return missionRecordService.findCompleteMissionRecords(missionId);
    }

    @Operation(summary = "미션 탭 상태 조회", description = "미션 탭의 상태를 조회한다.")
    @GetMapping("/status")
    public MissionTabResponse getMissionRecordStatus(@RequestParam Long missionId) {
        return missionRecordService.getMissionTabStatus(missionId);
    }

    @Operation(summary = "미션 참여", description = "미션 참여하기.")
    @PostMapping("/start")
    public MissionRecordIdResponse startMissionRecord(
            @Valid @RequestBody MissionRecordStartRequest request) {
        return missionRecordService.startMission(request.missionId());
    }

    @Operation(summary = "미션 기록 저장", description = "미션 완료 후 기록을 저장한다.")
    @PostMapping
    public ResponseEntity<Void> saveMissionRecord(
            @Valid @RequestBody MissionRecordSaveRequest request) {
        missionRecordService.saveMission(request.missionId(), request.content());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "미션 기록 삭제", description = "미션 기록을 삭제한다.")
    @DeleteMapping("/{recordId}")
    public void deleteMissionRecord(@PathVariable Long recordId) {
        missionRecordService.deleteMissionRecord(recordId);
    }

    @Operation(summary = "캘린더 형식의 미션 기록 조회", description = "회원의 미션 기록을 페이징하여 조회한다.")
    @GetMapping("/calendar")
    public MissionRecordCalendarResponse getMissionRecordsForCalendar(
            @Valid MissionRecordCalendarRequest request) {

        return missionRecordService.getMissionRecordsForCalendar(request);
    }

    @Operation(summary = "수행한 총 미션 기록 수", description = "회원이 수행한 총 미션 기록 수를 조회한다.")
    @GetMapping("/complete/total")
    public MissionRecordCompleteTotal getTotalMissionRecords(
            @Parameter(description = "조회할 memberId", example = "1")
                    @Valid
                    @RequestParam(required = false)
                    Long memberId) {
        return missionRecordService.getTotalMissionRecords(memberId);
    }

    @Operation(summary = "부스트 생성", description = "미션 기록에 부스트를 생성한다.")
    @PostMapping("/{recordId}/boost")
    public ResponseEntity<Void> createMissionRecordBoost(
            @PathVariable("recordId") Long recordId,
            final @Valid @RequestBody MissionRecordBoostRequest request) {
        missionRecordService.createBoost(recordId, request.count());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
