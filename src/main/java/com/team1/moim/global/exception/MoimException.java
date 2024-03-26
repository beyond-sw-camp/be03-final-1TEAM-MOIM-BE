package com.team1.moim.global.exception;

import lombok.Getter;

/**
 * 모든 커스텀 예외가 상속받는 상위 예외
 * 커스텀 예외를 여기서 상속받도록 하는 이유는 ErrorCode를 가지게 하기 위함
 */
@Getter
public class MoimException extends RuntimeException{

    private final ErrorCode errorCode;

    protected MoimException(ErrorCode errorCode){
        super();
        this.errorCode = errorCode;
    }
}
