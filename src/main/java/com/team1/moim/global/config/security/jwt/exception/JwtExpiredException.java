package com.team1.moim.global.config.security.jwt.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class JwtExpiredException extends MoimException {
    private static final ErrorCode errorCode = ErrorCode.JWT_EXPIRED;

    public JwtExpiredException(){
        super(errorCode);
    }
}
