package com.team1.moim.domain.auth.controller;

import com.team1.moim.domain.auth.service.AuthService;
import com.team1.moim.domain.auth.dto.request.SignUpRequest;
import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public ResponseEntity<ApiSuccessResponse<MemberResponse>> signUp(HttpServletRequest request,
                                                                     @Valid SignUpRequest signUpRequest){
        log.info("회원가입 API 시작");

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        authService.signUp(signUpRequest)
                ));
    }
}
