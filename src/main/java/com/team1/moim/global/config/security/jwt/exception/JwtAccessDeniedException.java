package com.team1.moim.global.config.security.jwt.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class JwtAccessDeniedException extends MoimException {
    private static final ErrorCode errorCode = ErrorCode.ACCESS_DENIED;

    public JwtAccessDeniedException(){
        super(errorCode);
    }
}
