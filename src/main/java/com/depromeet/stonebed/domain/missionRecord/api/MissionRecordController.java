package com.depromeet.stonebed.domain.missionRecord.api;

import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionCompleteRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCalendarRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionStartRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionTabRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionCompleteResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionStartResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionTabResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3. 미션 기록", description = "미션 기록 관련 API입니다.")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class MissionRecordController {

    private final MissionRecordService missionRecordService;

    @Operation(summary = "미션 탭 상태 조회", description = "미션 탭의 상태를 조회한다.")
    @PostMapping("/status")
    public MissionTabResponse getMissionTabStatus(@Valid @RequestBody MissionTabRequest request) {
        return missionRecordService.getMissionTabStatus(request);
    }

    @Operation(summary = "미션 참여", description = "미션 참여하기.")
    @PostMapping("/start")
    public MissionStartResponse startMission(@Valid @RequestBody MissionStartRequest request) {
        return missionRecordService.startMission(request);
    }

    @Operation(summary = "미션 완료", description = "미션 완료된 데이터를 조회한다.")
    @PostMapping("/complete")
    public MissionCompleteResponse completeMission(
            @Valid @RequestBody MissionCompleteRequest request) {
        return missionRecordService.completeMission(request);
    }

    @Operation(summary = "미션 기록 저장", description = "미션 완료 후 기록을 저장한다.")
    @PostMapping
    public MissionRecordCreateResponse saveMission(
            @Valid @RequestBody MissionRecordCreateRequest request) {
        return missionRecordService.saveMission(request);
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
}
