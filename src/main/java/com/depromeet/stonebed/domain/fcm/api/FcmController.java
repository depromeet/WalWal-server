package com.depromeet.stonebed.domain.fcm.api;

import com.depromeet.stonebed.domain.fcm.application.FcmNotificationService;
import com.depromeet.stonebed.domain.fcm.application.FcmScheduledService;
import com.depromeet.stonebed.domain.fcm.application.FcmTokenService;
import com.depromeet.stonebed.domain.fcm.dto.request.FcmTokenRequest;
import com.depromeet.stonebed.domain.fcm.dto.response.FcmNotificationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "6. [알림]", description = "알림 관련 API입니다.")
@RestController
@RequestMapping("/alarm")
@RequiredArgsConstructor
public class FcmController {
    private final FcmTokenService fcmTokenService;
    private final FcmNotificationService fcmNotificationService;
    private final FcmScheduledService fcmScheduledService;

    @Operation(summary = "FCM 토큰 저장", description = "로그인 시 FCM 토큰을 저장합니다.")
    @PostMapping("/token")
    public ResponseEntity<Void> fcmTokenStore(
            @RequestBody @Validated FcmTokenRequest fcmTokenRequest) {
        fcmTokenService.storeOrUpdateToken(fcmTokenRequest.token());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "FCM 토큰 삭제", description = "로그아웃 시 FCM 토큰을 삭제합니다.")
    @DeleteMapping("/token")
    public ResponseEntity<Void> fcmTokenDelete() {
        fcmTokenService.invalidateTokenForCurrentMember();
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "알림 리스트 조회", description = "회원의 알림을 커서 기반으로 페이징하여 조회한다.")
    @GetMapping
    public FcmNotificationResponse getFcmNotifications(
            @Valid @RequestParam(name = "cursor", required = false) String cursor,
            @Valid @NotNull @Min(1) @RequestParam(name = "limit", defaultValue = "10") int limit) {
        return fcmNotificationService.getNotificationsForCurrentMember(cursor, limit);
    }

    @Operation(summary = "FCM 알림 읽음 처리", description = "알림을 읽음 상태로 변경합니다.")
    @PostMapping("/{notificationId}/read")
    public ResponseEntity<Void> fcmNotificationAsRead(
            @PathVariable("notificationId") Long notificationId) {
        fcmNotificationService.markNotificationAsRead(notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/test/ala")
    public ResponseEntity<Void> testtets() {
        fcmScheduledService.sendReminderToIncompleteMissions();
        return ResponseEntity.ok().build();
    }
}
