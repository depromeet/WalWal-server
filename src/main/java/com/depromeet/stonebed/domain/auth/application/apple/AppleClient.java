package com.depromeet.stonebed.domain.auth.application.apple;

import com.depromeet.stonebed.domain.auth.dto.request.AppleTokenRequest;
import com.depromeet.stonebed.domain.auth.dto.response.AppleKeyListResponse;
import com.depromeet.stonebed.domain.auth.dto.response.AppleKeyResponse;
import com.depromeet.stonebed.domain.auth.dto.response.AppleTokenResponse;
import com.depromeet.stonebed.domain.auth.dto.response.SocialClientResponse;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.infra.properties.AppleProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.jwk.JWK;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@RequiredArgsConstructor
public class AppleClient {

    private final ObjectMapper objectMapper;
    private final RestClient restClient;
    private final AppleProperties appleProperties;
    private static final String APPLE_AUDIENCE = "https://appleid.apple.com";
    private static final String TOKEN_ENDPOINT = "https://appleid.apple.com/auth/token";
    private static final String KEY_ENDPOINT = "https://appleid.apple.com/auth/keys";

    public AppleTokenResponse getAppleToken(AppleTokenRequest appleTokenRequest) {
        return restClient
                .post()
                .uri(TOKEN_ENDPOINT)
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .body(appleTokenRequest)
                .exchange(
                        (request, response) -> {
                            if (!response.getStatusCode().is2xxSuccessful())
                                throw new CustomException(ErrorCode.UNKNOWN_SERVER_ERROR);
                            return Objects.requireNonNull(
                                    response.bodyTo(AppleTokenResponse.class));
                        });
    }

    public String makeClientSecret() throws IOException {
        Date expirationDate =
                Date.from(
                        LocalDateTime.now()
                                .plusDays(30)
                                .atZone(ZoneId.systemDefault())
                                .toInstant());
        return Jwts.builder()
                .setHeaderParam("kid", appleProperties.keyId())
                .setHeaderParam("alg", "ES256")
                // TODO: dev, prod 환경분리 필요
                .setIssuer(appleProperties.dev().teamId())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(expirationDate)
                .setAudience(APPLE_AUDIENCE)
                .setSubject(appleProperties.dev().teamId())
                .signWith(getPrivateKey())
                .compact();
    }

    private PrivateKey getPrivateKey() throws IOException {
        ClassPathResource resource = new ClassPathResource(appleProperties.p8());
        String privateKey = new String(Files.readAllBytes(Paths.get(resource.getURI())));
        Reader pemReader = new StringReader(privateKey);
        PEMParser pemParser = new PEMParser(pemReader);
        JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
        PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
        return converter.getPrivateKey(object);
    }

    /**
     * Apple로부터 받은 idToken 검증하고 identifier를 추출합니다.
     *
     * @param authorizationCode
     * @return
     */
    public SocialClientResponse authenticateFromApple(String authorizationCode) throws IOException {
        AppleTokenRequest tokenRequest =
                AppleTokenRequest.of(
                        authorizationCode,
                        appleProperties.dev().clientId(),
                        makeClientSecret(),
                        "authorization_code",
                        null);
        AppleTokenResponse appleTokenResponse = getAppleToken(tokenRequest);

        AppleKeyResponse[] keys = retrieveAppleKeys();
        try {
            String[] tokenParts = appleTokenResponse.id_token().split("\\.");
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

            Claims claims =
                    parseIdentifierFromAppleToken(matchedKey, appleTokenResponse.id_token());

            String identifier = claims.get("sub", String.class);
            String email = claims.get("email", String.class);

            return new SocialClientResponse(email, identifier);
        } catch (Exception ex) {
            throw new CustomException(ErrorCode.UNKNOWN_SERVER_ERROR);
        }
    }

    private AppleKeyResponse[] retrieveAppleKeys() {
        AppleKeyListResponse keyListResponse =
                restClient
                        .get()
                        .uri(KEY_ENDPOINT)
                        .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                        .exchange(
                                (request, response) -> {
                                    if (!response.getStatusCode().is2xxSuccessful())
                                        throw new CustomException(ErrorCode.UNKNOWN_SERVER_ERROR);
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

    // RSA 공개키 생성
    private PublicKey generatePublicKey(final AppleKeyResponse applePublicKey) {
        final byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.n());
        final byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.e());

        final BigInteger n = new BigInteger(1, nBytes);
        final BigInteger e = new BigInteger(1, eBytes);
        final RSAPublicKeySpec rsaPublicKeySpec = new RSAPublicKeySpec(n, e);

        try {
            final KeyFactory keyFactory = KeyFactory.getInstance(applePublicKey.kty());
            return keyFactory.generatePublic(rsaPublicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException exception) {
            throw new RuntimeException("Invalid Apple Public Key");
        }
    }
}
