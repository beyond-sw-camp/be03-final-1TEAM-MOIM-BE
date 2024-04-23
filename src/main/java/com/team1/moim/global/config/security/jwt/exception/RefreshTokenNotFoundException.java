package com.team1.moim.global.config.security.jwt.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class RefreshTokenNotFoundException extends MoimException {
    private static final ErrorCode errorCode = ErrorCode.REFRESH_TOKEN_NOT_FOUND;

    public RefreshTokenNotFoundException(){
        super(errorCode);
    }
}
