package com.depromeet.stonebed.domain.missionRecord.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.domain.missionRecord.dto.request.MissionRecordBoostRequest;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        value = MissionRecordController.class,
        includeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = AuthenticationEntryPoint.class)
        })
@ActiveProfiles("test")
public class MissionRecordControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private MissionRecordService missionRecordService;
    private final Gson gson = new Gson();

    @Test
    @WithMockUser
    void 부스트_성공() throws Exception {
        // Given: 요청
        MissionRecordBoostRequest missionRecordBoostRequest = new MissionRecordBoostRequest(10L);

        // When & Then: 요청 & 성공
        mockMvc.perform(
                        post("/records/1/boost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(missionRecordBoostRequest))
                                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    @WithMockUser
    void 부스트_실패_부스트_카운트가_500보다_큰_경우() throws Exception {
        // Given: 부스트 카운트가 501인 요청
        MissionRecordBoostRequest missionRecordBoostRequest = new MissionRecordBoostRequest(501L);

        // When & Then: 요청 & 응답이 400 Bad Request
        mockMvc.perform(
                        post("/records/1/boost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(missionRecordBoostRequest))
                                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @WithMockUser
    void 부스트_실패_부스트_카운트가_0인_경우() throws Exception {
        // Given: 부스트 카운트가 0인 요청
        MissionRecordBoostRequest missionRecordBoostRequest = new MissionRecordBoostRequest(0L);

        // When & Then: 요청 & 응답이 400 Bad Request
        mockMvc.perform(
                        post("/records/1/boost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(missionRecordBoostRequest))
                                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
