package com.team1.moim.domain.event.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;
import lombok.Getter;

@Getter
public class EventNotFoundException extends MoimException {

    private static final ErrorCode errorCode = ErrorCode.EVENT_NOT_FOUND;

    public EventNotFoundException(){
        super(errorCode);
    }
}
