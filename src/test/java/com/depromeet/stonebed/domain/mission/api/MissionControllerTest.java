package com.depromeet.stonebed.domain.mission.api;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.depromeet.stonebed.domain.mission.application.MissionService;
import com.depromeet.stonebed.domain.mission.dto.request.MissionCreateRequest;
import com.depromeet.stonebed.domain.mission.dto.request.MissionUpdateRequest;
import com.depromeet.stonebed.domain.mission.dto.response.MissionCreateResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetOneResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionGetTodayResponse;
import com.depromeet.stonebed.domain.mission.dto.response.MissionUpdateResponse;
import com.depromeet.stonebed.global.common.response.ApiResponseAdvice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

public class MissionControllerTest {

    private MockMvc mockMvc;

    @Mock private MissionService missionService;

    @InjectMocks private MissionController missionController;

    @InjectMocks private ApiResponseAdvice apiResponseAdvice;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc =
                MockMvcBuilders.standaloneSetup(missionController)
                        .setControllerAdvice(apiResponseAdvice)
                        .build();
    }

    @Test
    public void 미션_생성_성공() throws Exception {
        // Given
        MissionCreateResponse missionCreateResponse = new MissionCreateResponse(1L, "Test Mission");
        when(missionService.createMission(any(MissionCreateRequest.class)))
                .thenReturn(missionCreateResponse);

        // When & Then
        mockMvc.perform(
                        post("/missions")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Test Mission\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Mission"));
    }

    @Test
    public void 오늘의_미션_조회_성공() throws Exception {
        // Given
        MissionGetTodayResponse missionGetTodayResponse =
                new MissionGetTodayResponse(
                        1L, "Test Mission", "https://example.com/image.png", "#FFFFFF");
        when(missionService.getOrCreateTodayMission()).thenReturn(missionGetTodayResponse);

        // When & Then
        mockMvc.perform(get("/missions/today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Mission"));
    }

    @Test
    public void 미션_조회_성공() throws Exception {
        // Given
        MissionGetOneResponse missionGetOneResponse = new MissionGetOneResponse(1L, "Test Mission");
        when(missionService.getMission(1L)).thenReturn(missionGetOneResponse);

        // When & Then
        mockMvc.perform(get("/missions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Test Mission"));
    }

    @Test
    public void 미션_수정_성공() throws Exception {
        // Given
        when(missionService.updateMission(anyLong(), any(MissionUpdateRequest.class)))
                .thenReturn(new MissionUpdateResponse(1L, "Updated Mission"));

        // When & Then
        mockMvc.perform(
                        patch("/missions/1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"title\":\"Updated Mission\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Updated Mission"));
    }

    @Test
    public void 미션_삭제_성공() throws Exception {
        // Given
        doNothing().when(missionService).deleteMission(anyLong());

        // When & Then
        mockMvc.perform(delete("/missions/1")).andExpect(status().isOk());
    }
}
