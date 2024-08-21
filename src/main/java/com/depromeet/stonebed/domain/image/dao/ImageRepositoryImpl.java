package com.depromeet.stonebed.domain.image.dao;

import static com.depromeet.stonebed.domain.image.domain.QImage.*;

import com.depromeet.stonebed.domain.image.domain.Image;
import com.depromeet.stonebed.domain.image.domain.ImageFileExtension;
import com.depromeet.stonebed.domain.image.domain.ImageType;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ImageRepositoryImpl implements ImageRepositoryCustom {
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Optional<Image> findTopImageByTarget(
            ImageType imageType, Long targetId, ImageFileExtension imageFileExtension) {
        return Optional.ofNullable(
                jpaQueryFactory
                        .selectFrom(image)
                        .where(
                                eqImageType(imageType),
                                eqTargetId(targetId),
                                eqImageFileExtension(imageFileExtension))
                        .orderBy(image.id.desc())
                        .limit(1)
                        .fetchOne());
    }

    private BooleanExpression eqImageType(ImageType imageType) {
        return imageType != null ? image.imageType.eq(imageType) : null;
    }

    private BooleanExpression eqTargetId(Long targetId) {
        return targetId != null ? image.targetId.eq(targetId) : null;
    }

    private BooleanExpression eqImageFileExtension(ImageFileExtension imageFileExtension) {
        return imageFileExtension != null ? image.imageFileExtension.eq(imageFileExtension) : null;
    }
}
