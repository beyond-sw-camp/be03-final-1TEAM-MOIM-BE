package com.team1.moim.domain.group.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class HostIncludedException extends MoimException {

    private static final ErrorCode errorCode = ErrorCode.HOST_INCLUDED;

    public HostIncludedException() {
        super(errorCode);
    }
}
