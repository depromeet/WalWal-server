package com.depromeet.stonebed.domain.fcm.dao;

import java.util.List;

public interface FcmRepositoryCustom {
    List<String> findAllValidTokens();
}
