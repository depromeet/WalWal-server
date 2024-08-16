package com.depromeet.stonebed.domain.member.api;

import com.depromeet.stonebed.domain.member.application.MemberService;
import com.depromeet.stonebed.domain.member.domain.Member;
import com.depromeet.stonebed.domain.member.dto.request.MemberProfileUpdateRequest;
import com.depromeet.stonebed.domain.member.dto.request.NicknameCheckRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "회원 프로필 변경", description = "회원 프로필을 변경합니다.")
    @PutMapping("/me")
    public ResponseEntity<Void> memberProfileModify(
            @Valid @RequestBody MemberProfileUpdateRequest request) {
        memberService.modifyMemberProfile(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "닉네임 유효성 체크", description = "닉네임 유효성 체크를 진행합니다.")
    @PostMapping("/check-nickname")
    public ResponseEntity<Void> memberNicknameCheck(
            @Valid @RequestBody NicknameCheckRequest request) {
        memberService.checkNickname(request);
        return ResponseEntity.ok().build();
    }
}
