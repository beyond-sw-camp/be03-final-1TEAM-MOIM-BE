package com.team1.moim.global.config.security.login.handler;

import com.team1.moim.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;

import java.io.IOException;

/**
 * 커스텀한 JSON 로그인 필터를 통과하여 인증 실패가 되면 (로그인 실패)
 * 이 로그인 실패 핸들러가 동작
 */

@Slf4j
@RequiredArgsConstructor
public class LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/plain;charset=UTF-8");
        response.getWriter().write(ErrorCode.LOGIN_FAILED.getMessage());
        log.info("로그인에 실패했습니다. 메시지 : {}", exception.getMessage());
    }
}
