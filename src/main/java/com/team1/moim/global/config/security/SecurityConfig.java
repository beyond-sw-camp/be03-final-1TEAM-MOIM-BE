package com.team1.moim.global.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.security.jwt.JwtAuthenticationProcessingFilter;
import com.team1.moim.global.config.security.jwt.JwtProvider;
import com.team1.moim.global.config.security.login.filter.CustomJsonUsernamePasswordAuthenticationFilter;
import com.team1.moim.global.config.security.login.handler.LoginFailureHandler;
import com.team1.moim.global.config.security.login.handler.LoginSuccessHandler;
import com.team1.moim.global.config.security.login.service.LoginService;
import com.team1.moim.global.config.security.oauth2.handler.OAuth2LoginFailureHandler;
import com.team1.moim.global.config.security.oauth2.handler.OAuth2LoginSuccessHandler;
import com.team1.moim.global.config.security.oauth2.service.CustomOAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String[] EVENT_API_URL = {
            "/connect",
            "api/events",
            "api/events/**",
//            "api/events/repeat/"
    };
    private static final String[] AUTH_API_URL = {
            "/api/auth",
            "/api/auth/**"
    };
    private static final String[] MOIM_API_URL = {
            "/api/group",
            "/api/group/**",
    };

    private final LoginService loginService;
    private final JwtProvider jwtProvider;
    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;
    private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;
    private final OAuth2LoginFailureHandler oAuth2LoginFailureHandler;
    private final CustomOAuth2UserService customOAuth2UserService;


    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081"));
        configuration.setAllowedMethods(Arrays.asList("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Authorization_Refresh", "Cache-Control", "Content-Type"));
        configuration.setExposedHeaders(Arrays.asList("Authorization", "Authorization_Refresh"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity

                // 쿠키와 세션을 사용하는 MVC패턴에서 csrf 공격에 취약하나, REST API는 덜 취약하여 비활성화
                .csrf(AbstractHttpConfigurer::disable)

                // CORS 활성화(특정 도메인만 접속할 수 있도록 허용)
//                .cors(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .formLogin(AbstractHttpConfigurer::disable)

                // 기본 인증 로그인 사용 안하고 JWT 로그인 사용하므로 disable
                .httpBasic(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequest ->
                        authorizeRequest
                                .requestMatchers("/" ).permitAll()
                                .requestMatchers(EVENT_API_URL).permitAll()
                                .requestMatchers(AUTH_API_URL).permitAll()
                                .requestMatchers(MOIM_API_URL).permitAll()
                                .anyRequest()
                                .authenticated()
                )
                // 세션을 사용하지 않는 설정 추가
                .sessionManagement(sessionManagement ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(userInfoEndpoint -> userInfoEndpoint
                                .userService(customOAuth2UserService))
                        .successHandler(oAuth2LoginSuccessHandler)
                        .failureHandler(oAuth2LoginFailureHandler)
                )
                // Custom Filter 추가 (UsernamePasswordAuthenticationFilter 실행 전에 jwtAuthFilter를 실행)
                // addFilterAfter(A,B): B필터 이후에 A 필터가 동작하도록 하는 메서드
                .addFilterAfter(customJsonUsernamePasswordAuthenticationFilter(), LogoutFilter.class)

                // addFilterBefore(A,B): B필터 이전에 A필터가 동작하도록 하는 메서드
                .addFilterBefore(jwtAuthenticationProcessingFilter(), CustomJsonUsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(loginService);
        return new ProviderManager(provider);
    }

    @Bean
    public LoginSuccessHandler loginSuccessHandler() {
        return new LoginSuccessHandler(jwtProvider, memberRepository);
    }

    @Bean
    public LoginFailureHandler loginFailureHandler() {
        return new LoginFailureHandler();
    }

    @Bean
    public CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordAuthenticationFilter() {
        CustomJsonUsernamePasswordAuthenticationFilter customJsonUsernamePasswordLoginFilter
                = new CustomJsonUsernamePasswordAuthenticationFilter(objectMapper);
        customJsonUsernamePasswordLoginFilter.setAuthenticationManager(authenticationManager());
        customJsonUsernamePasswordLoginFilter.setAuthenticationSuccessHandler(loginSuccessHandler());
        customJsonUsernamePasswordLoginFilter.setAuthenticationFailureHandler(loginFailureHandler());
        return customJsonUsernamePasswordLoginFilter;
    }

    @Bean
    public JwtAuthenticationProcessingFilter jwtAuthenticationProcessingFilter() {
        return new JwtAuthenticationProcessingFilter(jwtProvider, memberRepository);
    }
}
