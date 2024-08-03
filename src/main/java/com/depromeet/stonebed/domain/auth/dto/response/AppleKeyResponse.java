package com.depromeet.stonebed.domain.auth.dto.response;

public record AppleKeyResponse(
        String kty, String kid, String use, String alg, String n, String e) {}
