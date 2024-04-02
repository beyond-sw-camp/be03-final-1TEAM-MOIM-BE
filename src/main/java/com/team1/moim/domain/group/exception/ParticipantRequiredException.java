package com.team1.moim.domain.group.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class ParticipantRequiredException extends MoimException {

    private static final ErrorCode errorcode = ErrorCode.PARTICIPANT_REQUIRED;

    public ParticipantRequiredException() {
        super(errorcode);
    }
}
