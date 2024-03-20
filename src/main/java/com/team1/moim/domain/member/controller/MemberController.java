package com.team1.moim.domain.member.controller;

import com.team1.moim.domain.member.dto.request.CreateMemberRequest;
import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.domain.member.service.MemberService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<MemberResponse> signUp(@Valid @RequestBody CreateMemberRequest request){
        return ResponseEntity.ok().body(memberService.signUp(request));
    }
}
