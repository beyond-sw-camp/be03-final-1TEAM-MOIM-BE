package com.team1.moim.domain.member.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class EmailNotFoundException extends MoimException {

    private static final ErrorCode errorCode = ErrorCode.EMAIL_NOT_FOUND;

    public EmailNotFoundException(){
        super(errorCode);
    }
}
