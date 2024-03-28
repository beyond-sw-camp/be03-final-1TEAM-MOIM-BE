package com.team1.moim.global.config.security.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.security.jwt.exception.JwtAccessDeniedException;
import com.team1.moim.global.config.security.jwt.exception.JwtExpiredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Getter
@Slf4j
public class JwtProvider {
    @Value("${jwt.secretKey}")
    private String secretKey;

    @Value("${jwt.access.expiration}")
    private Long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private Long refreshTokenExpiration;

    @Value("${jwt.access.header}")
    private String accessHeader;

    @Value("${jwt.refresh.header}")
    private String refreshHeader;

    private static final String ACCESS_TOKEN_SUBJECT = "AccessToken";
    private static final String REFRESH_TOKEN_SUBJECT = "RefreshToken";
    private static final String EMAIL_CLAIM = "email";
    private static final String ROLE_CLAIM = "role";
    private static final String BEARER = "Bearer ";

    private final ObjectMapper objectMapper;
    private final MemberRepository memberRepository;


    /**
     * AccessToken 생성 메서드
     * Payload에 email과 role 정보 넣어서 생성
     */
    public String createAccessToken(String email, String role){
        Date now = new Date();
        return JWT.create() // JWT 토큰을 생성하는 빌더 생성
                .withSubject(ACCESS_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + accessTokenExpiration))
                .withClaim(EMAIL_CLAIM, email) // 사용자 정의 클레임
                .withClaim(ROLE_CLAIM, role)
                .sign(Algorithm.HMAC512(secretKey)); // HMAC512 알고리즘 사용하여 secret 키로 암호화
    }

    /**
     * RefreshToken 생성 메서드
     * DB에 저장되어 있어 따로 Payload에 값을 가지고 있지 않아도 됨
     */
    public String createRefreshToken(){
        Date now = new Date();
        return JWT.create()
                .withSubject(REFRESH_TOKEN_SUBJECT)
                .withExpiresAt(new Date(now.getTime() + refreshTokenExpiration))
                .sign(Algorithm.HMAC512(secretKey));
    }

    /**
     * Response Body에 AccessToken 값을 담아 반환해주는 메서드
     * AccessToken이 재발급 될 때 사용
     * 형식: { "Authorization" : {token} }
     */
    public void sendAccessToken(HttpServletResponse response,
                                String accessToken) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Map<String, String> token = new HashMap<>();
        token.put(accessHeader, accessToken);

        // 헤더와 accessToken이 담긴 Map 형태의 데이터를 String으로 변환
        String result = objectMapper.writeValueAsString(token);
        log.info("Map 형태의 accessToken 정보를 String으로 변환: " + result);

        // 클라이언트에게 문자 형태로 응답을 하기 위함
        response.getWriter().write(result);
        log.info("재발급된 Access Token: {}", accessToken);
    }

    /**
     * 초기 로그인 성공 시 Response Body에 두 토큰의 값을 담아 반환
     * 형식: { "Authorization" : {AccessToken}, "AuthorizationRefresh" : {RefreshToken}  }
     */
    public void sendAccessAndRefreshToken(HttpServletResponse response,
                                          String accessToken,
                                          String refreshToken) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        Map<String, String> token = new HashMap<>();
        token.put(accessHeader, accessToken);
        token.put(refreshHeader, refreshToken);

        String result = objectMapper.writeValueAsString(token);
        log.info("Map 형태의 access, refreshToken 정보를 String으로 변환: " + result);

        response.getWriter().write(result);
        log.info("Access Token, Refresh Token 바디에 설정 완료");
    }

    /**
     * 헤더에서 RefreshToken 추출 후 토큰의 값이 DB에 있는지 확인
     * 헤더에 담긴 토큰 형식이 Bearer [토큰] 형식이므로 토큰 값을 가져오기 위해서는 Bearer를 제거해야함
     * 헤더에 담겨져 올 때, 앞에 "Bearer "이 붙어서 오기 때문에, 제거해주는 스트림 사용(""로 replace)
     */
    public Optional<String> extractRefreshToken(HttpServletRequest request){
        return Optional.ofNullable(request.getHeader(refreshHeader))
                .filter(refreshToken -> refreshToken.startsWith(BEARER))
                .map(refreshToken -> refreshToken.replace(BEARER, ""));
    }

    /**
     * 헤더에서 AccessToken 추출 후 토큰의 값이 DB에 있는지 확인
     * 헤더에 담겨져 올 때, 앞에 "Bearer "이 붙어서 오기 때문에, 제거해주는 스트림 사용(""로 replace)
     */
    public Optional<String> extractAccessToken(HttpServletRequest request) {
        return Optional.ofNullable(request.getHeader(accessHeader))
                .filter(accessToken -> accessToken.startsWith(BEARER))
                .map(accessToken -> accessToken.replace(BEARER, ""));
    }

    /**
     * AccessToken에서 Role 추출
     * 추출 전에 JWT.require()로 검증기 생성
     * verify로 AccessToken 검증 후
     * 유효하면 getClaim()으로 role 추출, 유효하지 않으면 예외 발생
     * 기존 JWT 토큰이 HS512 인코딩 방식을 사용했기 때문에,
     * 같은 방식으로 디코딩 후 DB에 있는 role과의 일치 여부를 확인하는 메서드
     * 에러 발생 시 401 UNAUTHORIZED 에러 발생
     */
    public Optional<String> extractEmail(String accessToken) throws IOException {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build() // 반환된 빌더로 JWT verifier 생성
                    .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getClaim(EMAIL_CLAIM) // claim(email) 가져오기
                    .asString());
        } catch (Exception e){
            throw new JwtExpiredException();
        }
    }

    public Optional<String> extractRole(String accessToken, HttpServletResponse response) throws IOException {
        try {
            return Optional.ofNullable(JWT.require(Algorithm.HMAC512(secretKey))
                    .build() // 반환된 빌더로 JWT verifier 생성
                    .verify(accessToken) // accessToken을 검증하고 유효하지 않다면 예외 발생
                    .getClaim(ROLE_CLAIM) // claim(Role) 가져오기
                    .asString());
        } catch (Exception e){
            throw new JwtAccessDeniedException();
        }
    }

    /**
     * 생성된 RefreshToken을 해당 유저의 DB에 추가
     * Oauth 로그인 성공 시 처리하는 LoginSuccessHandler에서 사용할 예정
     */
    public void updateRefreshToken(String email, String refreshToken){
        try {
            memberRepository.findByEmail(email)
                    .ifPresent(member -> member.updateRefreshToken(refreshToken));
        } catch (Exception e){
            throw new MemberNotFoundException();
        }
    }

    /**
     * 매 인증 시마다(클라이언트가 토큰을 헤더에 담아서 요청할 때마다) 토큰 검증 단계를 거치게 되는데,
     * 각 AccessToken, RefreshToken의 유효성을 검증할 때 사용한다.
     * 헤더의 토큰을 HS512 인증 방식을 통해 디코딩 후 유효성 확인
     */
    public boolean isTokenValid(String token){
        try {
            JWT.require(Algorithm.HMAC512(secretKey)).build().verify(token);
            return true;
        } catch (Exception e){
            log.error("유효하지 않은 토큰입니다. {}", e.getMessage());
            return false;
        }
    }
}
