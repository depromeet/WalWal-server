package com.depromeet.stonebed.domain.mission.api;

import com.depromeet.stonebed.domain.mission.application.MissionService;
import com.depromeet.stonebed.domain.mission.dto.MissionDTO;
import com.depromeet.stonebed.global.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/missions")
public class MissionController {
    private final MissionService missionService;

    @Autowired
    public MissionController(MissionService missionService) {
        this.missionService = missionService;
    }

    @PostMapping
    public ApiResponse createMission(@RequestBody MissionDTO missionDTO) {
        MissionDTO mission = missionService.createMission(missionDTO);
        return ApiResponse.success(HttpStatus.CREATED.value(), mission);
    }

    @GetMapping("/{id}")
    public ApiResponse getMission(@PathVariable Long id) {
        MissionDTO mission = missionService.getMission(id);
        return ApiResponse.success(HttpStatus.OK.value(), mission);
    }

    @PatchMapping("/{id}")
    public ApiResponse updateMission(
            @PathVariable Long id, @Valid @RequestBody MissionUpdateRequest missionUpdateRequest) {
        MissionDTO mission = missionService.updateMission(id, missionUpdateRequest);
        return ApiResponse.success(HttpStatus.OK.value(), mission);
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
        return ApiResponse.success(HttpStatus.NO_CONTENT.value(), null);
    }
}
