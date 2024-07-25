package com.depromeet.stonebed.domain.image.dao;

import com.depromeet.stonebed.domain.image.domain.Image;
import com.depromeet.stonebed.domain.image.domain.ImageFileExtension;
import com.depromeet.stonebed.domain.image.domain.ImageType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findTopByImageTypeAndTargetIdAndImageFileExtensionOrderById(
            ImageType imageType, Long targetId, ImageFileExtension imageFileExtension);
}
