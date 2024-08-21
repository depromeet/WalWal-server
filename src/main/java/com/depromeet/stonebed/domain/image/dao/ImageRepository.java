package com.depromeet.stonebed.domain.image.dao;

import com.depromeet.stonebed.domain.image.domain.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long>, ImageRepositoryCustom {}
