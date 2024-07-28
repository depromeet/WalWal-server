package com.depromeet.stonebed.domain.image.domain;

import com.depromeet.stonebed.domain.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "image_id")
    private Long id;

    @Enumerated(EnumType.STRING)
    private ImageType imageType;

    // targetId 예시: missionRecord와 같이 이미지를 가지는 대상의 id
    private Long targetId;

    @Column(length = 36)
    private String imageKey;

    @Enumerated(EnumType.STRING)
    private ImageFileExtension imageFileExtension;

    @Builder(access = AccessLevel.PRIVATE)
    private Image(
            Long id,
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        this.id = id;
        this.imageType = imageType;
        this.targetId = targetId;
        this.imageKey = imageKey;
        this.imageFileExtension = imageFileExtension;
    }

    public static Image createImage(
            ImageType imageType,
            Long targetId,
            String imageKey,
            ImageFileExtension imageFileExtension) {
        return Image.builder()
                .imageType(imageType)
                .targetId(targetId)
                .imageKey(imageKey)
                .imageFileExtension(imageFileExtension)
                .build();
    }
}
