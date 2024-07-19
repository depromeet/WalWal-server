package com.depromeet.stonebed.domain.missionRecord.api;

import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordDayRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCalendarResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordDayResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "미션 기록", description = "미션 기록 관련 API입니다.")
@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class MissionRecordController {

    private final MissionRecordService missionRecordService;

    @Operation(summary = "미션 기록 저장", description = "미션 완료 후 기록을 저장한다.")
    @PostMapping
    public MissionRecordCreateResponse completeMission(
            @Valid @RequestBody MissionRecordCreateRequest request) {
        return missionRecordService.completeMission(request);
    }

    @Operation(summary = "미션 기록 삭제", description = "미션 기록을 삭제한다.")
    @DeleteMapping("/{recordId}")
    public void deleteMissionRecord(@PathVariable Long recordId) {
        missionRecordService.deleteMissionRecord(recordId);
    }

    @Operation(summary = "캘린더 형식의 미션 기록 조회", description = "회원의 월별, 일별 미션 기록을 조회한다.")
    @GetMapping("/calendar")
    public MissionRecordCalendarResponse getMissionRecordsForCalendar() {
        return missionRecordService.getMissionRecordsForCalendar();
    }

    @Operation(summary = "특정 일자의 미션 기록 조회", description = "회원의 특정 일자에 해당하는 미션 기록을 조회한다.")
    @PostMapping("/calendar/day")
    public MissionRecordDayResponse getMissionRecordsForDay(
            @Valid @RequestBody MissionRecordDayRequest request) {
        return missionRecordService.getMissionRecordsForDay(request);
    }
}
