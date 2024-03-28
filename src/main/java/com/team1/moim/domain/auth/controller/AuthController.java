package com.team1.moim.domain.auth.controller;

import com.team1.moim.domain.auth.dto.request.LoginRequest;
import com.team1.moim.domain.auth.dto.response.LoginResponse;
import com.team1.moim.domain.auth.dto.response.EmailResponse;
import com.team1.moim.domain.auth.service.AuthService;
import com.team1.moim.domain.auth.dto.request.SignUpRequest;
import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.global.config.redis.RedisService;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.NoSuchAlgorithmException;

@RestController
@Slf4j
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final RedisService redisService;

    @Autowired
    public AuthController(AuthService authService, RedisService redisService){
        this.authService = authService;
        this.redisService = redisService;
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
  
    @PostMapping("/send-email")
    public ResponseEntity<ApiSuccessResponse<EmailResponse>> send(HttpServletRequest request,
                                                                  @RequestParam("email") String email) throws NoSuchAlgorithmException {
        String sentAuthCode;
        try {
            String createdCode = authService.generateRandomNumber();
            log.info("이메일 전송 api 시작");
            sentAuthCode = authService.sendEmailCode(email, createdCode);
        } catch (MailException e){
            throw new MailSendException("이메일 인증코드 전송에 실패했습니다.");
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        EmailResponse.from(email, sentAuthCode)
                ));
    }

    @PostMapping("/verify")
    public ResponseEntity<ApiSuccessResponse<String>> verify(HttpServletRequest request,
                                                           @RequestParam("email") String email,
                                                           @RequestParam("authCode") String authCode){
        String resultMessage;
        String authValue = redisService.getValues(email);
        boolean isAuthCheck = redisService.checkExistsValue(authValue);
        boolean isAuthEqual = authCode.equals(authValue);

        if (isAuthCheck){
            if (isAuthEqual){
                resultMessage = "이메일 인증이 완료되었습니다.";
                redisService.deleteValues(email);
            } else {
                resultMessage = "인증번호가 맞지 않습니다. 다시 확인해주세요";
            }
        } else {
            resultMessage = "이메일 인증시간이 만료되었습니다.";
        }

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        request.getServletPath(),
                        resultMessage
                ));
    }
}
