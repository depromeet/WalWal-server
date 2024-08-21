package com.depromeet.stonebed.domain.image.dao;

import com.depromeet.stonebed.domain.image.domain.Image;
import com.depromeet.stonebed.domain.image.domain.ImageFileExtension;
import com.depromeet.stonebed.domain.image.domain.ImageType;
import java.util.Optional;

public interface ImageRepositoryCustom {
    Optional<Image> findTopImageByTarget(
            ImageType imageType, Long targetId, ImageFileExtension imageFileExtension);
}
