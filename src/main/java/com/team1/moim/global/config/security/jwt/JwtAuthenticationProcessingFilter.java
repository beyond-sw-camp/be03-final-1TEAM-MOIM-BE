package com.team1.moim.global.config.security.jwt;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.security.oauth2.util.PasswordUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 클라이언트가 헤더에 JWT 토큰을 담아서 "/login" URL 이외의 요청을 보내면,
 * 해당 토큰들의 유효성을 검사하여 인증 처리/인증 실패/토큰 재발급 등을 수행
 * 1. 요청 헤더에 RT가 없고, AT가 유효한 경우 -> 인증 성공 처리, RT 재발급 X
 * 2. 요청 헤더에 RT가 없고, AT가 없거나 유효하지 않은 경우 -> 인증 실패 처리(403)
 * 3. 요청 헤더에 RT가 있는 경우 -> DB에 있는 RT와의 일치여부가 검증되면 AT, RT 재발급 (RTR 방식)
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationProcessingFilter extends OncePerRequestFilter {

    // 아래 url로 들어오는 요청은 Filter 작동 X
    private static final String[] NO_CHECK_URLS =
            {"/login",
            "/oauth2/authorization/google",
            "/login/oauth2/code/google",
            "/favicon.ico",
            "/api/auth/sign-up",
            "/api/auth/send",
            "/api/auth/verify",
            "/api/auth/email-validate",
            "/api/auth/nickname-validate"};

    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    /**
     * NO_CHECK_URLS로 요청이 들어오면, filterChain.doFilter()로 현재 필터 통과
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        log.info("JWT Filter 진입");
        log.info("requestURI: " + request.getRequestURI());
        for (String url : NO_CHECK_URLS){
            if (request.getRequestURI().equals(url)){
                log.info(url + " 필터 통과");
                filterChain.doFilter(request, response); // NO_CHECK_URLS로 요청 들어오면 다음 필터 호출
                return; // 이후 현재 필터 진행 막기 (안해주면 아래로 내려가서 계속 필터 진행 시킴)
            }
        }
        log.info("NO_CHECK_URL PASS");

        // 요청 헤더에서 RT 호출
        // RT가 없거나 유효하지 않으면(DB에 저장된 RT와 다르다면) null 반환
        // 요청 헤더에 RT가 있는 경우는, AT가 만료되어 요청한 경우밖에 없음
        // 따라서, 위의 경우를 제외하면 추출한 RT는 모두 null임
        String refreshToken = jwtProvider.extractRefreshToken(request)
                .filter(jwtProvider::isTokenValid)
                .orElse(null);

        log.info("Refresh Token 정보: {}", refreshToken);

        // RT가 요청 헤더에 존재하면, 사용자의 AT가 만료됐기 때문에,
        // RT까지 보낸 것이므로, 요청 헤더의 RT가 DB의 RT가 일치하는 지 검증하고,
        // 일치하면 AT 재발급
        if (refreshToken != null){
            checkRefreshTokenAndReIssueAccessToken(response, refreshToken);
            return; // RT를 보낸 경우, AT를 재발급하고 인증처리는 하지 않게 하기 위해 return으로 필터 진행 막음
        }

        checkAccessTokenAndAuthentication(request, response, filterChain);
    }

    /**
     * 요청 헤더에서 추출한 RT로 유저 정보를 찾고,
     * 유저가 있으면 AT 재발급해서 응답 헤더에 보냄
     */
    public void checkRefreshTokenAndReIssueAccessToken(HttpServletResponse response, String refreshToken){
        memberRepository.findByRefreshToken(refreshToken)
                .ifPresent(member -> {
                    String reIssuedAccessToken = jwtProvider.createAccessToken(member.getEmail(),
                                                                                member.getRole().name());
                    try {
                        jwtProvider.sendAccessToken(response, reIssuedAccessToken);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    /**
     *
     */
    public void checkAccessTokenAndAuthentication(HttpServletRequest request, HttpServletResponse response,
                                                  FilterChain filterChain) throws ServletException, IOException {
        log.info("checkAccessTokenAndAuthentication() 호출");
        jwtProvider.extractAccessToken(request)
                .filter(jwtProvider::isTokenValid)
                .ifPresent(accessToken -> {
                    try {
                        jwtProvider.extractEmail(accessToken)
                                .ifPresent(email -> memberRepository.findByEmail(email)
                                        .ifPresent(this::saveAuthentication)
                                );
                    } catch (Exception e){
                        throw new RuntimeException(e);
                    }
                });
        log.info("Authentication 객체에 대한 인증 허가 처리 완료");
        filterChain.doFilter(request, response);
    }

    /**
     * 인증 허가 메서드
     * 파라미터의 member: 우리가 만든 회원 객체 / Builder의 유저: UserDetails의 User 객체
     * 소셜 로그인을 한 유저의 경우, 비밀번호 값을 받아오지 못하기 때문에,
     * passwordUtil을 이용해 자체 랜덤 비밀번호 생성
     * SecurityContextHolder.getContext()로 SecurityContext를 꺼낸 후,
     * setAuthentication()을 이용하여 위에서 만든 Authentication 객체에 대한 인증 허가 처리
     */
    public void saveAuthentication(Member currentMember){
        String password = currentMember.getPassword();
        if (password == null){ // 소셜 로그인 유저의 비밀번호 임의로 설정 하여 소셜 로그인 유저도 인증 되도록 설정
            password = PasswordUtil.generateRandomPassword();
        }

        UserDetails userDetails = User.builder()
                .username(currentMember.getEmail())
                .password(password)
                .roles(currentMember.getRole().name())
                .build();

        // UsernamePasswordAuthenticationToken으로, Authentication 객체 생성
        // 두번째 파라미터(credential)는 보통 비밀번호로, 인증 시에는 보통 null로 제거
        Authentication authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null,
                        authoritiesMapper.mapAuthorities(userDetails.getAuthorities()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("saveAuthentication() 종료");
    }

    /**
     * RT를 재발급 해주는 메서드
     * RT 만료 시에는 에러 메시지를 발송하여 클라이언트를 로그아웃 처리하므로, 사용 안함.
     * 즉, RT 재발급 자체를 안함.
     */
    private String reIssueRefreshToken(Member member){
        String reIssuedRefreshToken = jwtProvider.createRefreshToken();
        member.updateRefreshToken(reIssuedRefreshToken);
        memberRepository.saveAndFlush(member);

        return reIssuedRefreshToken;
    }

}
