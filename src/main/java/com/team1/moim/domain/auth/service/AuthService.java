package com.team1.moim.domain.auth.service;

import com.team1.moim.domain.auth.dto.request.SignUpRequest;
import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.entity.Role;
import com.team1.moim.domain.member.exception.*;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.redis.RedisService;
import com.team1.moim.global.config.s3.S3Service;
import com.team1.moim.global.config.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private static final String FILE_TYPE = "members";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;
    private final JavaMailSender javaMailSender;
    private final MailProperties mailProperties;
    private final RedisService redisService;


    @Transactional
    public MemberResponse signUp(SignUpRequest request) {
        if (memberRepository.findByNickname(request.getNickname()).isPresent()) {
            throw new NicknameDuplicateException();
        }
        if (memberRepository.findByEmail(request.getEmail()).isPresent()){
            throw new EmailDuplicationException();
        }
        String imageUrl = null;
        if (request.getProfileImage() != null && !request.getProfileImage().isEmpty()){
            imageUrl = s3Service.uploadFile(FILE_TYPE, request.getProfileImage());
        } else {
            imageUrl = s3Service.getDefaultImage(FILE_TYPE);
        }
        Member newMember = request.toEntity(passwordEncoder, Role.USER, imageUrl);

        return MemberResponse.from(memberRepository.save(newMember));
    }

    public void sendEmail(String from, String to, String title, String contents){
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setFrom(from);
        mailMessage.setTo(to);
        mailMessage.setSubject(title);
        mailMessage.setText(contents);

        javaMailSender.send(mailMessage);
    }

    public String sendEmailCode(String email, String authCode) {
        Duration duration = Duration.ofMinutes(3);
        redisService.setValues(email, authCode, duration);

        sendEmail(mailProperties.getUsername(), email, "MOIM에서 발송한 인증번호를 확인해주세요.", authCode);

        return authCode;
    }

    public String generateRandomNumber() throws NoSuchAlgorithmException {
        String result;

        do {
            int num = SecureRandom.getInstanceStrong().nextInt(999999);
            result = String.valueOf(num);
        } while (result.length() != 6);

        return result;
    }
}
