package com.team1.moim.domain.auth.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class NotFoundCodeException extends MoimException {

    private static final ErrorCode errorcode = ErrorCode.NOT_FOUND_CODE;

    public NotFoundCodeException() {
        super(errorcode);
    }
}
