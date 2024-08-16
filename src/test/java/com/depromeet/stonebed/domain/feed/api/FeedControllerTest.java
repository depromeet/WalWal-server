package com.depromeet.stonebed.domain.feed.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.depromeet.stonebed.domain.feed.application.FeedService;
import com.depromeet.stonebed.domain.feed.dto.request.FeedBoostRequest;
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
        value = FeedController.class,
        includeFilters = {
            @ComponentScan.Filter(
                    type = FilterType.ASSIGNABLE_TYPE,
                    classes = AuthenticationEntryPoint.class)
        })
@ActiveProfiles("test")
public class FeedControllerTest {
    @Autowired private MockMvc mockMvc;

    @MockBean private FeedService feedService;

    private final Gson gson = new Gson();

    @Test
    @WithMockUser
    void 부스트_성공() throws Exception {
        // Given: 요청
        FeedBoostRequest feedBoostRequest = new FeedBoostRequest(10L, 1L);

        // When & Then: 요청 & 성공
        mockMvc.perform(
                        post("/feed/boost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(feedBoostRequest))
                                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isCreated())
                .andReturn();
    }

    @Test
    @WithMockUser
    void 부스트_실패_부스트_카운트가_500보다_큰_경우() throws Exception {
        // Given: 부스트 카운트가 501인 요청
        FeedBoostRequest feedBoostRequest = new FeedBoostRequest(501L, 1L);

        // When & Then: 요청 & 응답이 400 Bad Request
        mockMvc.perform(
                        post("/feed/boost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(feedBoostRequest))
                                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @WithMockUser
    void 부스트_실패_부스트_카운트가_0인_경우() throws Exception {
        // Given: 부스트 카운트가 0인 요청
        FeedBoostRequest feedBoostRequest = new FeedBoostRequest(0L, 1L);

        // When & Then: 요청 & 응답이 400 Bad Request
        mockMvc.perform(
                        post("/feed/boost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(feedBoostRequest))
                                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }

    @Test
    @WithMockUser
    void 부스트_실패_미션기록_고유번호가_1보다_작은_경우() throws Exception {
        // Given: 미션 기록 ID가 0인 요청
        FeedBoostRequest feedBoostRequest = new FeedBoostRequest(10L, 0L);

        // When & Then: 요청 & 응답이 400 Bad Request
        mockMvc.perform(
                        post("/feed/boost")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(gson.toJson(feedBoostRequest))
                                .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isBadRequest())
                .andReturn();
    }
}
