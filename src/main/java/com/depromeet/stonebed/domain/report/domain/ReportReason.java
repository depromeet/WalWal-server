package com.depromeet.stonebed.domain.report.domain;

import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import java.util.Arrays;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ReportReason {
    HARASSMENT_OR_ABUSE("사기 또는 사칭"),
    NOT_A_PET("반려동물이 아님"),
    VIOLENCE_HARASSMENT_OR_HATE("폭력, 혐오 또는 학대"),
    ADVERTISEMENT_SPAM("광고, 홍보, 스팸"),
    ADULT_CONTENT("성인용 콘텐츠"),
    OTHER("기타");

    private final String value;

    public static ReportReason fromName(String name) {
        return Arrays.stream(values())
                .filter(reason -> reason.name().equals(name))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.INVALID_REPORT_REASON));
    }

    public static String getValueFromName(String name) {
        return fromName(name).getValue();
    }
}
