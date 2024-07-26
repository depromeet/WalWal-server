package com.depromeet.stonebed.domain.image.application;

import com.depromeet.stonebed.domain.image.dao.ImageRepository;
import com.depromeet.stonebed.infra.properties.S3Properties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ImageService {

    private final ImageRepository imageRepository;
    private final S3Properties s3Properties;
}
