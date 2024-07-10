package com.depromeet.stonebed.domain.auth.application.apple;

import com.depromeet.stonebed.domain.auth.dto.response.AppleKeyListResponse;
import com.depromeet.stonebed.domain.auth.dto.response.AppleKeyResponse;
import com.depromeet.stonebed.domain.auth.dto.response.IdentifierResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.security.InvalidParameterException;
import java.security.Key;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AppleClient {

    private final ObjectMapper objectMapper;

    public IdentifierResponse authenticateFromApple(String accessToken) {
        AppleKeyResponse[] keys = retrieveAppleKeys();
        try {
            String[] tokenParts = accessToken.split("\\.");
            String headerPart = new String(Base64.getDecoder().decode(tokenParts[0]));
            JsonNode headerNode = objectMapper.readTree(headerPart);
            String kid = headerNode.get("kid").asText();

            AppleKeyResponse matchedKey =
                    Arrays.stream(keys)
                            .filter(key -> key.kid().equals(kid))
                            .findFirst()
                            // 일치하는 키가 없음 => 만료된 토큰 or 이상한 토큰 => throw
                            .orElseThrow(InvalidParameterException::new);

            String identifier = parseIdentifierFromAppleToken(matchedKey, accessToken);
            return new IdentifierResponse(identifier);
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.UNKNOWN_SERVER_ERROR);
        }
    }

    private AppleKeyResponse[] retrieveAppleKeys() {
        RestClient restClient = RestClient.create();

        AppleKeyListResponse keyListResponse =
                restClient
                        .get()
                        .uri("https://appleid.apple.com/auth/keys")
                        .header(
                                HttpHeaders.CONTENT_TYPE,
                                "application/x-www-form-urlencoded;charset=utf-8")
                        .exchange(
                                (request, response) -> {
                                    if (!response.getStatusCode().is2xxSuccessful())
                                        throw new CustomException(ErrorCode.UNKNOWN_SERVER_ERROR);
                                    return Objects.requireNonNull(
                                            response.bodyTo(AppleKeyListResponse.class));
                                });

        return keyListResponse.keys();
    }

    private String parseIdentifierFromAppleToken(AppleKeyResponse matchedKey, String accessToken)
            throws JsonProcessingException, ParseException, JOSEException {
        Key keyData =
                JWK.parse(objectMapper.writeValueAsString(matchedKey)).toRSAKey().toRSAPublicKey();
        Jws<Claims> parsedClaims =
                Jwts.parserBuilder().setSigningKey(keyData).build().parseClaimsJws(accessToken);

        return parsedClaims.getBody().get("sub", String.class);
    }
}
