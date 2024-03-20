package com.team1.moim.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ExceptionCode {

    // Member
    EMAIL_DUPLICATION(400, "이미 존재하는 이메일입니다."),
    EMAIL_NOT_FOUND(400, "존재하지 않는 이메일입니다."),
    MEMBER_NOT_FOUND(400, "존재하지 않는 회원입니다."),
    PASSWORD_NOT_MATCH(400, "비밀번호가 일치하지 않습니다."),
    NICKNAME_DUPLICATION( 400, "이미 존재하는 닉네임입니다.");

    private final int status;
    private final String message;
}
