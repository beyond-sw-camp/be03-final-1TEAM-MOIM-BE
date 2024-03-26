package com.team1.moim.domain.auth.service;

import com.team1.moim.domain.auth.dto.request.LoginRequest;
import com.team1.moim.domain.auth.dto.request.SignUpRequest;
import com.team1.moim.domain.auth.dto.response.LoginResponse;
import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.entity.Role;
import com.team1.moim.domain.member.exception.*;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.s3.S3Service;
import com.team1.moim.global.config.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private static final String FILE_TYPE = "members";

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final S3Service s3Service;

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
        }
        Member newMember = request.toEntity(passwordEncoder, Role.ROLE_USER, imageUrl);

        return MemberResponse.from(memberRepository.save(newMember));
    }

    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        // Email 존재 여부 Check
        Member member = memberRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(EmailNotFoundException::new);

        // 계정 삭제 여부 Check
        if(member.getDeleteYn().equals("Y")) {
            throw new MemberNotFoundException();
        }

        // Password 일치 여부 Check
        if (!passwordEncoder.matches(loginRequest.getPassword(), member.getPassword())){
            throw new PasswordNotMatchException();
        }

        String token = jwtProvider.createToken(member.getEmail(),
                member.getRole().toString());

        return LoginResponse.builder()
                .id(member.getId())
                .token(token)
                .build();
    }
}
