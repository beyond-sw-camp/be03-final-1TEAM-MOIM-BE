package com.team1.moim.domain.group.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class GroupInfoNotFoundException extends MoimException {

    private static final ErrorCode errorcode = ErrorCode.GROUP_INFO_NOT_FOUND;

    public GroupInfoNotFoundException() {
        super(errorcode);
    }
}
