package com.depromeet.stonebed.domain.mission.api;

import com.depromeet.stonebed.domain.mission.application.MissionService;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetOneResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetTodayResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionUpdateResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "2. [미션]", description = "미션 관련 API입니다.")
@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {
    private final MissionService missionService;

    @PostMapping
    public MissionCreateResponse createMission(
            @Valid @RequestBody MissionCreateRequest missionCreateRequest) {
        return missionService.createMission(missionCreateRequest);
    }

    @GetMapping("/today")
    public MissionGetTodayResponse getTodayMission() {
        return missionService.getOrCreateTodayMission();
    }

    @GetMapping("/{missionId}")
    public MissionGetOneResponse getMission(@PathVariable("missionId") Long missionId) {
        return missionService.getMission(missionId);
    }

    @PatchMapping("/{missionId}")
    public MissionUpdateResponse updateMission(
            @PathVariable("missionId") Long missionId,
            @Valid @RequestBody MissionUpdateRequest missionUpdateRequest) {
        return missionService.updateMission(missionId, missionUpdateRequest);
    }

    @DeleteMapping("/{missionId}")
    public void deleteMission(@PathVariable("missionId") Long missionId) {
        missionService.deleteMission(missionId);
    }
}
