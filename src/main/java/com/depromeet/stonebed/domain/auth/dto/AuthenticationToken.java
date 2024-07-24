package com.depromeet.stonebed.domain.auth.dto;

import com.depromeet.stonebed.domain.auth.domain.TokenType;

public record AuthenticationToken(String userId, TokenType tokenType, String provider) {}
