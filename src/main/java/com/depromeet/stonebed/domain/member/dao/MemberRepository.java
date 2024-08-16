package com.depromeet.stonebed.domain.member.dao;

import com.depromeet.stonebed.domain.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
    Optional<Member> findByOauthInfoOauthProviderAndOauthInfoOauthId(
            String oauthProvider, String oauthId);

    boolean existsByProfileNickname(String nickname);
}
