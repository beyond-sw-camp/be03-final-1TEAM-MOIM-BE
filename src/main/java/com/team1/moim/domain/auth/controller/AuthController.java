package com.team1.moim.domain.auth.controller;

import com.team1.moim.domain.auth.dto.request.SignUpRequest;
import com.team1.moim.domain.auth.service.AuthService;
import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;

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

    // 토큰 재발급용 api
    // 아무 것도 실행 안함
    @GetMapping("/reissue")
    public void reIssueToken(){}
  
    @PostMapping("/send")
    public ResponseEntity<ApiSuccessResponse<String>> send(HttpServletRequest request,
                                                                  @RequestParam("email") String email) throws NoSuchAlgorithmException {
        log.info("이메일 전송 api 시작");
        try {
            authService.sendEmailCode(email);
        } catch (MailException e){
            throw new MailSendException("이메일 인증코드 전송에 실패했습니다.");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        "해당 이메일로 인증 코드가 발송되었습니다."
                ));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiSuccessResponse<String>> verify(HttpServletRequest request,
                                                           @RequestParam("email") String email,
                                                           @RequestParam("authCode") String authCode){

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        authService.verify(email, authCode)
                ));
    }

    // 이메일 중복 검증
    @PostMapping("/email-validate")
    public ResponseEntity<ApiSuccessResponse<String>> validateEmail(HttpServletRequest request,
                                                                  @RequestParam("email") String email){
        authService.validateEmail(email);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        "사용 가능한 이메일입니다."
                ));
    }

    // 닉네임 중복 검증
    @PostMapping("/nickname-validate")
    public ResponseEntity<ApiSuccessResponse<String>> validateNickname(HttpServletRequest request,
                                                                    @RequestParam("nickname") String nickname){
        authService.validateNickname(nickname);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        "사용 가능한 닉네임입니다."
                ));
    }
}
