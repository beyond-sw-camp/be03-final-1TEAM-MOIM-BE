package com.team1.moim.global.exception;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ErrorDetail {
    private final String type;
    private final String message;

    public static ErrorDetail of(String type, ErrorCode errorCode){
        return new ErrorDetail(type, errorCode.getMessage());
    }
}
