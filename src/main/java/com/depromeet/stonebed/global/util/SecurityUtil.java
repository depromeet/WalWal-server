package com.depromeet.stonebed.global.util;

import com.depromeet.stonebed.domain.auth.domain.TokenType;
import com.depromeet.stonebed.domain.auth.dto.AuthenticationToken;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    public Long getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            return Long.parseLong(authentication.getName());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_NOT_FOUND);
        }
    }

    public TokenType getTokenType() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        try {
            if (authentication.getCredentials() instanceof AuthenticationToken token) {
                return token.tokenType();
            } else {
                throw new CustomException(ErrorCode.AUTH_NOT_FOUND);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.AUTH_NOT_FOUND);
        }
    }
}
