package com.team1.moim.global.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // MOIM
    MOIM_NOT_FOUND(400, "존재하지 않는 모임입니다.");

    private int status;
    private String message;

    ErrorCode(final int status, final String message) {
        this.status = status;
        this.message = message;
    }
}
