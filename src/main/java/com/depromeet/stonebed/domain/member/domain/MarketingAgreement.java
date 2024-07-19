package com.depromeet.stonebed.domain.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MarketingAgreement {
    AGREED("AGREE"),
    DISAGREED("DISAGREED");

    private final String value;
}
