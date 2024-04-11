package com.team1.moim.domain.group.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class GroupAndGroupInfoNotMatchException extends MoimException {

    private static final ErrorCode errorcode = ErrorCode.GROUP_AND_GROUP_INFO_NOT_MATCH;

    public GroupAndGroupInfoNotMatchException() {
        super(errorcode);
    }
}
