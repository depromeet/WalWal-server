package com.depromeet.stonebed.domain.member.dao;

import com.depromeet.stonebed.domain.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {}
