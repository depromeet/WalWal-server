package com.depromeet.stonebed.global.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class SecurityUtilTest {

    @Mock private SecurityContext securityContext;

    @Mock private Authentication authentication;

    private SecurityUtil securityUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        securityUtil = new SecurityUtil();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void getCurrentMemberId_Success() {
        // given
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("123");

        // when
        Long memberId = securityUtil.getCurrentMemberId();

        // then
        assertEquals(123L, memberId);
    }

    @Test
    void getCurrentMemberId_Failure() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> {
                            securityUtil.getCurrentMemberId();
                        });
        assertEquals(ErrorCode.AUTH_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void getCurrentMemberRole_Failure() {
        // given
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        CustomException exception =
                assertThrows(
                        CustomException.class,
                        () -> {
                            securityUtil.getCurrentMemberRole();
                        });
        assertEquals(ErrorCode.AUTH_NOT_FOUND, exception.getErrorCode());
    }
}
