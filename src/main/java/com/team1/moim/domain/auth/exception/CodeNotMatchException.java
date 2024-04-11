package com.team1.moim.domain.auth.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class CodeNotMatchException extends MoimException {

    private static final ErrorCode errorcode = ErrorCode.CODE_NOT_MATCH;

    public CodeNotMatchException() {
        super(errorcode);
    }
}
