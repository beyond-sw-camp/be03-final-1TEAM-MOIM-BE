package com.team1.moim.domain.notification.exception;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.MoimException;

public class NotificationNotFoundException extends MoimException {

    private static final ErrorCode errorCode = ErrorCode.NOTIFICATION_NOT_FOUND;

    public NotificationNotFoundException(){
        super(errorCode);
    }
}
