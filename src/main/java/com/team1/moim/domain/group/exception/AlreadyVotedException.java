package com.team1.moim.domain.group.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class AlreadyVotedException extends MoimException {

    private static final ErrorCode errorcode = ErrorCode.ALREADY_VOTED;

    public AlreadyVotedException() {
        super(errorcode);
    }
}
