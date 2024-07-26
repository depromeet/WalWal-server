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
import com.depromeet.stonebed.domain.image.dto.response.PresignedUrlResponse;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.domain.Profile;
import com.depromeet.stonebed.global.common.constants.UrlConstants;
import com.depromeet.stonebed.global.error.ErrorCode;
import com.depromeet.stonebed.global.error.exception.CustomException;
import com.depromeet.stonebed.global.util.MemberUtil;
import com.depromeet.stonebed.global.util.SpringEnvironmentUtil;
import com.depromeet.stonebed.infra.properties.S3Properties;
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

    public PresignedUrlResponse createMemberProfilePresignedUrl(
            MemberProfileImageCreateRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();

        String imageKey = generateUUID();
        String fileName =
                createFileName(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        imageKey,
                        request.imageFileExtension());
        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                createGeneratePreSignedUrlRequest(
                        s3Properties.bucket(),
                        fileName,
                        request.imageFileExtension().getUploadExtension());

        String presignedUrl = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
        imageRepository.save(
                Image.createImage(
                        ImageType.MEMBER_PROFILE,
                        currentMember.getId(),
                        imageKey,
                        request.imageFileExtension()));
        return PresignedUrlResponse.from(presignedUrl);
    }

    public void uploadCompleteMemberProfile(MemberProfileImageUploadCompleteRequest request) {
        final Member currentMember = memberUtil.getCurrentMember();
        String imageUrl = null;
        if (request.imageFileExtension() != null) {
            Image image =
                    findImage(
                            ImageType.MEMBER_PROFILE,
                            currentMember.getId(),
                            request.imageFileExtension());
            imageUrl =
                    createReadImageUrl(
                            ImageType.MEMBER_PROFILE,
                            currentMember.getId(),
                            image.getImageKey(),
                            request.imageFileExtension());
        }
        // 현재 닉네임을 그대로 사용
        String currentNickname = currentMember.getProfile().getNickname();
        currentMember.updateProfile(Profile.createProfile(currentNickname, imageUrl));
    }

    private Image findImage(
            ImageType imageType, Long targetId, ImageFileExtension imageFileExtension) {
        return imageRepository
                .findTopByImageTypeAndTargetIdAndImageFileExtensionOrderById(
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
        return springEnvironmentUtil.getCurrentProfile()
                + "/"
                + imageType.getValue()
                + "/"
                + targetId
                + "/"
                + imageKey
                + "."
                + imageFileExtension.getUploadExtension();
    }

    private String createUploadImageUrl(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return s3Properties.endpoint()
                + "/"
                + s3Properties.bucket()
                + "/"
                + springEnvironmentUtil.getCurrentProfile()
                + "/"
                + imageType.getValue()
                + "/"
                + targetId
                + "/"
                + imageKey
                + "."
                + imageFileExtension.getUploadExtension();
    }

    private String createReadImageUrl(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return UrlConstants.IMAGE_DOMAIN_URL.getValue()
                + "/"
                + springEnvironmentUtil.getCurrentProfile()
                + "/"
                + imageType.getValue()
                + "/"
                + targetId
                + "/"
                + imageKey
                + "."
                + imageFileExtension.getUploadExtension();
    }

    private GeneratePresignedUrlRequest createGeneratePreSignedUrlRequest(
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
        Date expiration = new Date();
        var expTimeMillis = expiration.getTime();
        expTimeMillis += 1000 * 60 * 30;
        expiration.setTime(expTimeMillis);
        return expiration;
    }
}
