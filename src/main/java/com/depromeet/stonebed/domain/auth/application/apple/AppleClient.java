package com.depromeet.stonebed.domain.auth.application.apple;

import static com.depromeet.stonebed.global.common.constants.SecurityConstants.*;

import com.depromeet.stonebed.domain.auth.dto.request.AppleTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.response.AppleKeyListResponse;
import com.depromeet.stonebed.domain.auth.dto.response.AppleKeyResponse;
import com.depromeet.stonebed.domain.auth.dto.response.AppleTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.SpringEnvironmentUtil;
import com.depromeet.stonebed.infra.properties.AppleProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.PrivateKey;
import java.security.Security;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AppleClient {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AppleProperties appleProperties;
    private final SpringEnvironmentUtil springEnvironmentUtil;

    private static final int APPLE_TOKEN_EXPIRE_MINUTES = 5;

    /**
     * Apple로부터 받은 idToken 검증하고 identifier를 추출합니다.
     *
     * @param authorizationCode
     * @return
     */
    public SocialClientResponse authenticateFromApple(String authorizationCode) {
        AppleTokenRequest tokenRequest =
                AppleTokenRequest.of(
                        authorizationCode,
                        springEnvironmentUtil.isProdProfile()
                                ? appleProperties.prod().clientId()
                                : appleProperties.dev().clientId(),
                        generateAppleClientSecret(),
                        APPLE_GRANT_TYPE);
        AppleTokenResponse appleTokenResponse = getAppleToken(tokenRequest);

        AppleKeyResponse[] keys = retrieveAppleKeys();
        try {
            String[] tokenParts = appleTokenResponse.idToken().split("\\.");
            String headerPart = new String(Base64.getDecoder().decode(tokenParts[0]));
            JsonNode headerNode = objectMapper.readTree(headerPart);
            String kid = headerNode.get("kid").asText();
            String alg = headerNode.get("alg").asText();

            AppleKeyResponse matchedKey =
                    Arrays.stream(keys)
                            .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                            .findFirst()
                            // 일치하는 키가 없음 => 만료된 토큰 or 이상한 토큰 => throw
                            .orElseThrow(InvalidParameterException::new);

            Claims claims = parseIdentifierFromAppleToken(matchedKey, appleTokenResponse.idToken());

            String oauthId = claims.get("sub", String.class);
            String email = claims.get("email", String.class);

            return new SocialClientResponse(email, oauthId);
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }

    // apple server에서 받아온 id_token
    private AppleTokenResponse getAppleToken(AppleTokenRequest appleTokenRequest) {
        // Prepare form data
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", appleTokenRequest.clientId());
        formData.add("client_secret", appleTokenRequest.clientSecret());
        formData.add("code", appleTokenRequest.code());
        formData.add("grant_type", appleTokenRequest.grantType());

        return restClient
                .post()
                .uri(APPLE_TOKEN_URL)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .body(formData)
                .exchange(
                        (request, response) -> {
                            if (!response.getStatusCode().is2xxSuccessful()) {
                                throw new CustomException(ErrorCode.APPLE_TOKEN_CLIENT_FAILED);
                            }
                            return Objects.requireNonNull(
                                    response.bodyTo(AppleTokenResponse.class));
                        });
    }

    private String generateAppleClientSecret() {
        Date expirationDate =
                Date.from(
                        LocalDateTime.now()
                                .plusMinutes(APPLE_TOKEN_EXPIRE_MINUTES)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());

        String teamId =
                springEnvironmentUtil.isProdProfile()
                        ? appleProperties.prod().teamId()
                        : appleProperties.dev().teamId();
        String clientId =
                springEnvironmentUtil.isProdProfile()
                        ? appleProperties.prod().clientId()
                        : appleProperties.dev().clientId();
        return Jwts.builder()
                .setHeaderParam("kid", appleProperties.keyId())
                .setHeaderParam("alg", "ES256")
                // TODO: dev, prod 환경분리 필요
                .setIssuer(teamId.split("\\.")[0])
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience(APPLE_ISSUER)
                .setSubject(clientId)
                .signWith(getPrivateKey(), SignatureAlgorithm.ES256)
                .compact();
    }

    private PrivateKey getPrivateKey() {
        Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter().setProvider("BC");

        try {
            byte[] privateKeyBytes = Base64.getDecoder().decode(appleProperties.p8());
            PrivateKeyInfo privateKeyInfo = PrivateKeyInfo.getInstance(privateKeyBytes);
            return converter.getPrivateKey(privateKeyInfo);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.APPLE_PRIVATE_KEY_ENCODING_FAILED);
        }
    }

    private AppleKeyResponse[] retrieveAppleKeys() {
        AppleKeyListResponse keyListResponse =
                restClient
                        .get()
                        .uri(APPLE_JWK_SET_URL)
                        .header(HttpHeaders.CONTENT_TYPE, APPLICATION_URLENCODED)
                        .exchange(
                                (request, response) -> {
                                    if (!response.getStatusCode().is2xxSuccessful())
                                        throw new CustomException(
                                                ErrorCode.APPLE_TOKEN_CLIENT_FAILED);
                                    return Objects.requireNonNull(
                                            response.bodyTo(AppleKeyListResponse.class));
                                });

        return keyListResponse.keys();
    }

    private Claims parseIdentifierFromAppleToken(AppleKeyResponse matchedKey, String accessToken)
            throws JsonProcessingException, ParseException, JOSEException {
        Key keyData =
                JWK.parse(objectMapper.writeValueAsString(matchedKey)).toRSAKey().toRSAPublicKey();
        Jws<Claims> parsedClaims =
                Jwts.parserBuilder().setSigningKey(keyData).build().parseClaimsJws(accessToken);

        return parsedClaims.getBody();
    }
}
