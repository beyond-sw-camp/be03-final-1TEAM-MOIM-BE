package com.team1.moim.domain.member.controller;

import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.domain.member.service.MemberService;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;

    @Autowired
    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<MemberResponse>> view(HttpServletRequest request) {

        log.info("View Member Start");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        memberService.view()
                ));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping
    public ResponseEntity<ApiSuccessResponse<String>> delete(HttpServletRequest request){
        log.info("Delete Member Start");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        memberService.delete()
                ));
    }

    //멤버 검색
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/search")
    public ResponseEntity<ApiSuccessResponse<List<MemberResponse>>> searchMember(HttpServletRequest request) {

        log.info("멤버 검색 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        memberService.searchMember()));
    }

}
