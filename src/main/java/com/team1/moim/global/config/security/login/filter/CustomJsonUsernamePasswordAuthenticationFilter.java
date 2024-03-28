package com.team1.moim.global.config.security.login.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Spring Security Form Login 기반의 UsernamePasswordAuthenticationFilter를 참고하여 만든 커스텀 필터
 * "/login" 요청 왔을 때, JSON 값을 매핑 처리하는 필터 작동
 */
public class CustomJsonUsernamePasswordAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    private static final String DEFAULT_LOGIN_REQUEST_URL = "/login"; // "/login"으로 오는 요청을 처리
    private static final String HTTP_METHOD = "POST"; // 로그인 HTTP 메소드는 POST
    private static final String CONTENT_TYPE = "application/json"; // JSON 타입의 데이터로 오는 로그인 요청만 처리
    private static final String USERNAME_KEY = "email"; // 회원 로그인 시 이메일 요청 JSON Key : "email"
    private static final String PASSWORD_KEY = "password"; // 회원 로그인 시 비밀번호 요청 JSon Key : "password"
    private static final AntPathRequestMatcher DEFAULT_LOGIN_PATH_REQUEST_MATCHER =
            new AntPathRequestMatcher(DEFAULT_LOGIN_REQUEST_URL, HTTP_METHOD); // "/login" + POST로 온 요청에 매칭된다.


    private final ObjectMapper objectMapper;

    public CustomJsonUsernamePasswordAuthenticationFilter(ObjectMapper objectMapper) {
        super(DEFAULT_LOGIN_PATH_REQUEST_MATCHER); // 위에서 설정한 "login" + POST로 온 요청을 처리하기 위해 설정
        this.objectMapper = objectMapper;
    }

    /**
     * 인증 처리 메서드
     * AbstractAuthenticationProcessingFilter(부모)의 getAuthenticationManager()로 AuthenticationManager 객체를 반환 받은 후,
     * authenticate()의 파라미터로 UsernamePasswordAuthenticationToken 객체를 넣고 인증 처리
     * 여기서 AuthenticationManager 객체는 ProviderManager -> SecurityConfig에서 설정
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
                                                    throws AuthenticationException, IOException {
        if (request.getContentType() == null || !request.getContentType().equals(CONTENT_TYPE)) {
            throw new AuthenticationServiceException("Authentication Content-Type not supported: "
                                                        + request.getContentType());
        }

        // StreamUtils를 통해 request에서 messageBody(JSON) 반환
        // 요청 JSON example
        //  {"email" : "user@naver.com", "password" : "user1234@"}
        String messageBody = StreamUtils.copyToString(request.getInputStream(), StandardCharsets.UTF_8);

        // 꺼낸 messageBody를 objectMapper.readValue()로 Map으로 변환 (Key : JSON의 키 -> email, password)
        Map<String, String> usernamePasswordMap = objectMapper.readValue(messageBody, Map.class);

        // Map의 Key(email, password)로 해당 이메일, 패스워드 추출
        String email = usernamePasswordMap.get(USERNAME_KEY);
        String password = usernamePasswordMap.get(PASSWORD_KEY);

        // principal과 credentials을 AuthenticationManager(ProviderManager)에게 전달
        // ProviderManager가 전달받은 UsernamePasswordAuthenticationToken를
        // ProviderManager의 구현체인 DaoAuthenticationProvider로 전달
        // DaoAuthenticationProvider는 UserDetailsService의 loadUserByUsername을 호출하여
        // UserDetails 객체를 반환 받음 -> LoginService에서 추가 설명
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(email, password);

        return this.getAuthenticationManager().authenticate(authRequest);
    }
}
