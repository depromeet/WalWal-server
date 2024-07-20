package com.depromeet.stonebed.domain.member.api;

import com.depromeet.stonebed.domain.member.application.MemberService;
import com.depromeet.stonebed.domain.member.domain.Member;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "1-2. [회원]", description = "회원 관련 API입니다.")
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "내 정보 조회", description = "내 정보를 조회하는 API입니다.")
    @GetMapping("/me")
    public Member memberInfo() {
        return memberService.findMemberInfo();
    }
}
