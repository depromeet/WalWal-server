package com.depromeet.stonebed.domain.missionRecord.api;

import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordCreateRequest;
import com.depromeet.stonebed.domain.missionRecord.dto.response.MissionRecordCreateResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/records")
@RequiredArgsConstructor
public class MissionRecordController {

    private final MissionRecordService missionRecordService;

    @Operation(summary = "미션 기록 저장", description = "미션 완료 후 기록을 저장한다.")
    @PostMapping("/complete")
    public ResponseEntity<MissionRecordCreateResponse> completeMission(
            @Valid @RequestBody MissionRecordCreateRequest request) {

        MissionRecordCreateResponse response = missionRecordService.completeMission(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
