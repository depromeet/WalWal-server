package com.depromeet.stonebed.domain.mission.api;

import com.depromeet.stonebed.domain.mission.application.MissionService;
import com.depromeet.stonebed.domain.mission.dto.MissionDTO;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionUpdateResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
@Tag(name = "Mission API", description = "미션 API")
public class MissionController {
    private final MissionService missionService;

    @PostMapping
    public MissionCreateResponse createMission(
            @RequestBody MissionCreateRequest missionCreateRequest) {
        MissionDTO mission = missionService.createMission(missionCreateRequest);
        return new MissionCreateResponse(mission.id(), mission.title());
    }

    @GetMapping("/{missionId}")
    public MissionGetResponse getMission(@PathVariable Long missionId) {
        MissionDTO mission = missionService.getMission(missionId);
        return new MissionGetResponse(mission.id(), mission.title());
    }

    @PatchMapping("/{id}")
    public MissionUpdateResponse updateMission(
            @PathVariable Long id, @Valid @RequestBody MissionUpdateRequest missionUpdateRequest) {
        MissionDTO mission = missionService.updateMission(id, missionUpdateRequest);
        return new MissionUpdateResponse(mission.id(), mission.title());
    }

    @DeleteMapping("/{id}")
    public void deleteMission(@PathVariable Long id) {
        missionService.deleteMission(id);
    }
}
