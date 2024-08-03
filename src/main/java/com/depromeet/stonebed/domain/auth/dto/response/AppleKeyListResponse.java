package com.depromeet.stonebed.domain.auth.dto.response;

import java.util.Arrays;
import java.util.Optional;

public record AppleKeyListResponse(AppleKeyResponse[] keys) {
    @Override
    public boolean equals(Object obj) {
        return obj instanceof AppleKeyListResponse applekeylistresponse
                && Arrays.equals(applekeylistresponse.keys, keys);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(keys);
    }

    @Override
    public String toString() {
        return "AppleKeyListResponse{" + "keys=" + Arrays.toString(keys) + '}';
    }

    public Optional<AppleKeyResponse> getMatchedKeyBy(String kid, String alg) {
        return Arrays.stream(keys)
                .filter(key -> key.kid().equals(kid) && key.alg().equals(alg))
                .findFirst();
    }
}
