package com.depromeet.stonebed.domain.image.application;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.Headers;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.depromeet.stonebed.domain.image.dao.ImageRepository;
import com.depromeet.stonebed.domain.image.domain.Image;
import com.depromeet.stonebed.domain.image.domain.ImageFileExtension;
import com.depromeet.stonebed.domain.image.domain.ImageType;
import com.depromeet.stonebed.domain.image.dto.request.MemberProfileImageCreateRequest;
import com.depromeet.stonebed.domain.image.dto.request.MemberProfileImageUploadCompleteRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionImageCreateRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionImageUploadRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionRecordImageCreateRequest;
import com.depromeet.stonebed.domain.image.dto.request.MissionRecordImageUploadRequest;
import com.depromeet.stonebed.domain.image.dto.response.PresignedUrlResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.domain.mission.application.MissionService;
import com.depromeet.stonebed.domain.missionRecord.application.MissionRecordService;
import com.depromeet.stonebed.global.common.constants.UrlConstants;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.depromeet.stonebed.global.util.SpringEnvironmentUtil;
import com.depromeet.stonebed.infra.properties.S3Properties;
import java.net.URL;
import java.util.Date;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3;
    private final MemberUtil memberUtil;
    private final S3Properties s3Properties;
    private final SpringEnvironmentUtil springEnvironmentUtil;
    private final MissionRecordService missionRecordService;
    private final MissionService missionService;

    public PresignedUrlResponse createMemberProfilePresignedUrl(
            MemberProfileImageCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        validateImageFileExtension(request.imageFileExtension());
        String imageKey = generateUUID();
        String fileName =
                createFileName(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        imageKey,
                        request.imageFileExtension());
        GeneratePresignedUrlRequest presignedUrlRequest =
                createPreSignedUrlRequest(
                        s3Properties.bucket(),
                        fileName,
                        request.imageFileExtension().getUploadExtension());

        URL presignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest);
        imageRepository.save(
                Image.createImage(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        imageKey,
                        request.imageFileExtension()));
        return PresignedUrlResponse.from(presignedUrl.toString());
    }

    public void uploadCompleteMemberProfile(MemberProfileImageUploadCompleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        validateImageFileExtension(request.imageFileExtension());

        Image image =
                findImage(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        request.imageFileExtension());
        String imageUrl =
                createReadImageUrl(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        image.getImageKey(),
                        request.imageFileExtension());
        currentMember.updateProfile(Profile.createProfile(request.nickname(), imageUrl));
    }

    public PresignedUrlResponse createMissionRecordPresignedUrl(
            MissionRecordImageCreateRequest request) {
        validateImageFileExtension(request.imageFileExtension());
        String imageKey = generateUUID();

        Long missionRecordId = request.recordId();

        String fileName =
                createFileName(
                        ImageType.MISSION_RECORD,
                        missionRecordId,
                        imageKey,
                        request.imageFileExtension());
        GeneratePresignedUrlRequest presignedUrlRequest =
                createPreSignedUrlRequest(
                        s3Properties.bucket(),
                        fileName,
                        request.imageFileExtension().getUploadExtension());

        URL presignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest);
        imageRepository.save(
                Image.createImage(
                        ImageType.MISSION_RECORD,
                        missionRecordId,
                        imageKey,
                        request.imageFileExtension()));
        return PresignedUrlResponse.from(presignedUrl.toString());
    }

    public void uploadCompleteMissionRecord(MissionRecordImageUploadRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        validateImageFileExtension(request.imageFileExtension());

        Image image =
                findImage(
                        ImageType.MISSION_RECORD,
                        currentMember.getId(),
                        request.imageFileExtension());
        String imageUrl =
                createReadImageUrl(
                        ImageType.MISSION_RECORD,
                        currentMember.getId(),
                        image.getImageKey(),
                        request.imageFileExtension());

        missionRecordService.updateMissionRecordWithImage(request.recordId(), imageUrl);
    }

    public PresignedUrlResponse createMissionPresignedUrl(MissionImageCreateRequest request) {
        validateImageFileExtension(request.imageFileExtension());
        String imageKey = generateUUID();

        Long missionId = request.missionId();

        String fileName =
                createFileName(
                        ImageType.MISSION, missionId, imageKey, request.imageFileExtension());
        GeneratePresignedUrlRequest presignedUrlRequest =
                createPreSignedUrlRequest(
                        s3Properties.bucket(),
                        fileName,
                        request.imageFileExtension().getUploadExtension());

        URL presignedUrl = amazonS3.generatePresignedUrl(presignedUrlRequest);
        imageRepository.save(
                Image.createImage(
                        ImageType.MISSION, missionId, imageKey, request.imageFileExtension()));
        return PresignedUrlResponse.from(presignedUrl.toString());
    }

    public void uploadCompleteMission(MissionImageUploadRequest request) {
        validateImageFileExtension(request.imageFileExtension());

        Image image =
                findImage(ImageType.MISSION, request.missionId(), request.imageFileExtension());
        String imageUrl =
                createReadImageUrl(
                        ImageType.MISSION,
                        request.missionId(),
                        image.getImageKey(),
                        request.imageFileExtension());

        missionService.updateMissionWithImageUrl(request.missionId(), imageUrl);
    }

    private void validateImageFileExtension(ImageFileExtension imageFileExtension) {
        if (imageFileExtension == null) {
            throw new CustomException(ErrorCode.IMAGE_FILE_EXTENSION_NOT_FOUND);
        }
        try {
            ImageFileExtension.of(imageFileExtension.getUploadExtension());
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_IMAGE_FILE_EXTENSION);
        }
    }

    private Image findImage(
            ImageType imageType, Long targetId, ImageFileExtension imageFileExtension) {
        return imageRepository
                .findTopByImageTypeAndTargetIdAndImageFileExtensionOrderByIdDesc(
                        imageType, targetId, imageFileExtension)
                .orElseThrow(() -> new CustomException(ErrorCode.IMAGE_KEY_NOT_FOUND));
    }

    private String generateUUID() {
        return UUID.randomUUID().toString();
    }

    private String createFileName(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return String.format(
                "%s/%s/%d/%s.%s",
                springEnvironmentUtil.getCurrentProfile(),
                imageType.getValue(),
                targetId,
                imageKey,
                imageFileExtension.getUploadExtension());
    }

    private String createReadImageUrl(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return String.format(
                "%s/%s/%s/%d/%s.%s",
                UrlConstants.IMAGE_DOMAIN_URL.getValue(),
                springEnvironmentUtil.getCurrentProfile(),
                imageType.getValue(),
                targetId,
                imageKey,
                imageFileExtension.getUploadExtension());
    }

    private GeneratePresignedUrlRequest createPreSignedUrlRequest(
            String bucket, String fileName, String fileExtension) {
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(bucket, fileName, HttpMethod.PUT)
                        .withKey(fileName)
                        .withContentType("image/" + fileExtension)
                        .withExpiration(getPreSignedUrlExpiration());

        generatePresignedUrlRequest.addRequestParameter(
                Headers.S3_CANNED_ACL, CannedAccessControlList.PublicRead.toString());

        return generatePresignedUrlRequest;
    }

    private Date getPreSignedUrlExpiration() {
        return new Date(System.currentTimeMillis() + 1000 * 60 * 30);
    }
}
