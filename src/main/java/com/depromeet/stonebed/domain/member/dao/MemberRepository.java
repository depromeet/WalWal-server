package com.depromeet.stonebed.domain.member.dao;

import com.depromeet.stonebed.domain.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long>, MemberRepositoryCustom {

    @Query(
            "SELECT m FROM Member m WHERE m.oauthInfo.oauthProvider = :provider AND m.oauthInfo.oauthEmail = :email")
    Optional<Member> findByMemberOauthInfo(
            @Param("provider") String oauthProvider, @Param("email") String email);

    Optional<Member> findByOauthInfoOauthProviderAndOauthInfoOauthEmail(
            String oauthProvider, String email);
}
