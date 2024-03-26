package com.team1.moim.domain.member.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class MemberNotFoundException extends MoimException {

    private static final ErrorCode errorCode = ErrorCode.MEMBER_NOT_FOUND;

    public MemberNotFoundException(){
        super(errorCode);
    }
}
