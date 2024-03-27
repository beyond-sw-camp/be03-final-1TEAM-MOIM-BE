package com.team1.moim.global.config.security.jwt;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.ErrorDetail;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


@Component
public class JwtAuthFilter extends GenericFilter {

    @Value("${jwt.secretKey}")
    private String secretKey;

    @Override
    // request 객체 안에 토큰이 들어가 있다.
    public void doFilter(ServletRequest request,
                         ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        try {
            String bearerToken = ((HttpServletRequest) request).getHeader("Authorization");
            if (bearerToken != null) {
                if (!bearerToken.startsWith("Bearer ")) {
                    throw new AuthenticationServiceException("Token의 형식이 맞지 않습니다.");
                }
                // bearer 토큰에서 토큰 값만 추출
                String token = bearerToken.substring(7);
                System.out.println(token);

                // 토큰 검증 및 Claims 추출
                Claims claims = Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();

                // Authentication 객체를 생성하기 위한 UserDetails 생성
                List<GrantedAuthority> authorities = new ArrayList<>();
                authorities.add(new SimpleGrantedAuthority(claims.get("role").toString()));
                UserDetails userDetails = new User(claims.getSubject(), "", authorities);

                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, "", userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // FilterChain에서 그 다음 Filtering으로 넘어가도록 하는 메서드
            chain.doFilter(request, response);
        } catch (ExpiredJwtException e){
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(ErrorCode.JWT_EXPIRED.getStatus().value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write(ErrorDetail.from(ErrorCode.JWT_EXPIRED).toString());
        } catch (AuthenticationServiceException e) {
            HttpServletResponse httpServletResponse = (HttpServletResponse) response;
            httpServletResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
            httpServletResponse.setContentType("application/json");
            httpServletResponse.getWriter().write(ErrorDetail.from(ErrorCode.ACCESS_DENIED).toString());
        }
    }
}
