package com.team1.moim.global.config.security.login.service;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 커스텀 로그인 서비스
 */
@Service
@RequiredArgsConstructor
public class LoginService implements UserDetailsService {

    private final MemberRepository memberRepository;

    // UserDetails의 User 객체를 만들어서 반환하는 메서드
    // 반환받은 UserDetails 객체의 password를 꺼내어, 내부의 PasswordEncoder에서 password가 일치하는 지 검증 수행
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // DaoAuthenticationProvider가 설정해준 email을 가진 유저를 찾는다.
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

        return User.builder()
                .username(member.getEmail())
                .password(member.getPassword())
                // roles의 메서드를 보면 파라미터로 들어온 role들이 "ROLE_"으로 시작하지 않으면, 예외를 발생시킴
                .roles(member.getRole().name())
                .build();
    }
}
