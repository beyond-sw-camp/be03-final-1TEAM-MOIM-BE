package com.team1.moim.domain.member.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class GroupInfoNotFoundException extends MoimException {

    private static final ErrorCode errorCode = ErrorCode.GROUP_INFO_NOT_FOUND;

    public GroupInfoNotFoundException(){
        super(errorCode);
    }
}
