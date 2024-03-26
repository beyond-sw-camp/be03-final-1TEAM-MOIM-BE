package com.team1.moim.domain.member.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class EmailDuplicationException extends MoimException {

    private static final ErrorCode errorCode = ErrorCode.EMAIL_DUPLICATION;

    public EmailDuplicationException(){
        super(errorCode);
    }
}
