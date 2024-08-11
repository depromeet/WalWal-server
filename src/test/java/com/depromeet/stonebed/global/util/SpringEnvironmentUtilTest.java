package com.depromeet.stonebed.global.util;

import static com.depromeet.stonebed.global.common.constants.EnvironmentConstants.Constants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;

@ExtendWith(MockitoExtension.class)
class SpringEnvironmentUtilTest {
    private static final String[] PROD_ARRAY = new String[] {PROD_ENV};
    private static final String[] DEV_ARRAY = new String[] {DEV_ENV};
    private static final String[] LOCAL_ARRAY = new String[] {LOCAL_ENV};
    @Mock private Environment environment;
    @InjectMocks private SpringEnvironmentUtil springEnvironmentUtil;

    @Test
    void 상용_환경이라면_isProdProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(PROD_ARRAY);

        // when
        boolean result = springEnvironmentUtil.isProdProfile();

        // then
        assertTrue(result);
    }

    @Test
    void 상용_환경이_아니라면_isProdProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(DEV_ARRAY);

        // when
        boolean result = springEnvironmentUtil.isProdProfile();

        // then
        assertFalse(result);
    }

    @Test
    void 테스트_환경이라면_isDevProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(DEV_ARRAY);

        // when
        boolean result = springEnvironmentUtil.isDevProfile();

        // then
        assertTrue(result);
    }

    @Test
    void 테스트_환경이_아니라면_isDevProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(LOCAL_ARRAY);

        // when
        boolean result = springEnvironmentUtil.isDevProfile();

        // then
        assertFalse(result);
    }

    @Test
    void 상용_또는_테스트_환경이라면_isProdAndDevProfile은_true를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(new String[] {PROD_ENV, DEV_ENV});

        // when
        boolean result = springEnvironmentUtil.isProdAndDevProfile();

        // then
        assertTrue(result);
    }

    @Test
    void 상용_또는_테스트_환경이_아니라면_isProdAndDevProfile은_false를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(LOCAL_ARRAY);

        // when
        boolean result = springEnvironmentUtil.isProdAndDevProfile();

        // then
        assertFalse(result);
    }

    @Test
    void 상용_환경이라면_getCurrentProfile는은_prod를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(PROD_ARRAY);

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), PROD_ENV);
    }

    @Test
    void 테스트_환경이라면_getCurrentProfile는은_dev를_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(DEV_ARRAY);

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), DEV_ENV);
    }

    @Test
    void 로컬_환경이라면_getCurrentProfile는은_local을_반환한다() {
        // given
        given(environment.getActiveProfiles()).willReturn(LOCAL_ARRAY);

        // when
        // then
        assertEquals(springEnvironmentUtil.getCurrentProfile(), LOCAL_ENV);
    }
}
