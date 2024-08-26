package com.depromeet.stonebed.domain.auth.application.kakao;

import static com.depromeet.stonebed.global.common.constants.SecurityConstants.*;

import com.depromeet.stonebed.domain.auth.dto.response.KakaoAuthResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class KakaoClient {
    private final RestClient restClient;

    public SocialClientResponse authenticateFromKakao(String token) {
        KakaoAuthResponse kakaoAuthResponse =
                restClient
                        .get()
                        .uri(KAKAO_USER_ME_URL)
                        .header("Authorization", TOKEN_PREFIX + token)
                        .exchange(
                                (request, response) -> {
                                    if (!response.getStatusCode().is2xxSuccessful()) {
                                        throw new CustomException(
                                                ErrorCode.KAKAO_TOKEN_CLIENT_FAILED);
                                    }
                                    return Objects.requireNonNull(
                                            response.bodyTo(KakaoAuthResponse.class));
                                });

        return new SocialClientResponse(
                kakaoAuthResponse.kakaoAccount().email(), kakaoAuthResponse.id().toString());
    }
}
