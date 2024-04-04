package com.team1.moim.domain.group.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class ParticipantInfoNotMatchException extends MoimException {

    private static final ErrorCode errorcode = ErrorCode.PARTICIPANT_INFO_NOT_MATCH;

    public ParticipantInfoNotMatchException() {
        super(errorcode);
    }
}
