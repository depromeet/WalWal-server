package com.depromeet.stonebed.domain.auth.dao;

import com.depromeet.stonebed.domain.auth.domain.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRepository extends CrudRepository<RefreshToken, Long> {}
