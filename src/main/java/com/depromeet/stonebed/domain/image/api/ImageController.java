package com.depromeet.stonebed.domain.image.api;

import com.depromeet.stonebed.domain.image.application.ImageService;
import com.depromeet.stonebed.domain.image.dto.request.MemberProfileImageCreateRequest;
import com.depromeet.stonebed.domain.image.dto.request.MemberProfileImageUploadCompleteRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionImageCreateRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionImageUploadRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionRecordImageUploadRequest;
import com.depromeet.stonebed.domain.image.dto.response.ImageUrlResponse;
import com.depromeet.stonebed.domain.image.dto.response.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "3. [이미지]", description = "이미지 관련 API입니다.")
@RestController
@RequestMapping("/images")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;

    @Operation(
            summary = "회원 프로필 이미지 Presigned URL 생성",
            description = "회원 프로필 이미지 Presigned URL을 생성합니다.")
    @PostMapping("/members/me/upload-url")
    public PresignedUrlResponse memberProfilePresignedUrlCreate(
            @Valid @RequestBody MemberProfileImageCreateRequest request) {
        return imageService.createMemberProfilePresignedUrl(request);
    }

    @Operation(summary = "회원 프로필 이미지 업로드 완료", description = "회원 프로필 이미지 업로드 완료 업로드 상태를 변경합니다.")
    @PostMapping("/members/me/upload-complete")
    public ImageUrlResponse memberProfileUploaded(
            @Valid @RequestBody MemberProfileImageUploadCompleteRequest request) {
        return imageService.uploadCompleteMemberProfile(request);
    }

    @Operation(
            summary = "미션 기록 이미지 Presigned URL 생성",
            description = "미션 기록 이미지 Presigned URL을 생성합니다.")
    @PostMapping("/mission-record/upload-url")
    public PresignedUrlResponse missionRecordPresignedUrlCreate(
            @Valid @RequestBody MissionRecordImageCreateRequest request) {
        return imageService.createMissionRecordPresignedUrl(request);
    }

    @Operation(summary = "미션 기록 이미지 업로드 완료", description = "미션 기록 이미지 업로드 완료 업로드 상태를 변경합니다.")
    @PostMapping("/mission-record/upload-complete")
    public ImageUrlResponse missionRecordUploaded(
            @Valid @RequestBody MissionRecordImageUploadRequest request) {
        return imageService.uploadCompleteMissionRecord(request);
    }

    @Operation(
            summary = "미션 일러스트 이미지 Presigned URL 생성",
            description = "미션 일러스트 이미지 Presigned URL을 생성합니다.")
    @PostMapping("/mission/upload-url")
    public PresignedUrlResponse missionPresignedUrlCreate(
            @Valid @RequestBody MissionImageCreateRequest request) {
        return imageService.createMissionPresignedUrl(request);
    }

    @Operation(summary = "미션 일러스트 이미지 업로드 완료", description = "미션 일러스트 이미지 업로드 완료 업로드 상태를 변경합니다.")
    @PostMapping("/mission/upload-complete")
    public ImageUrlResponse missionUploaded(@Valid @RequestBody MissionImageUploadRequest request) {
        return imageService.uploadCompleteMission(request);
    }
}
