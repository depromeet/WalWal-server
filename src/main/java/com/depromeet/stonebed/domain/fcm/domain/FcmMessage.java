package com.depromeet.stonebed.domain.fcm.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FcmMessage {
    private String title;
    private String body;
    private String token;
}
