package com.depromeet.stonebed.domain.fcm.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FcmResponseErrorType {
    NOT_REGISTERED("NotRegistered"),
    INVALID_REGISTRATION("InvalidRegistration");
    private final String value;

    public static boolean contains(String responseBody, FcmResponseErrorType errorType) {
        return responseBody != null && responseBody.contains(errorType.getValue());
    }
}
