package com.team1.moim.global.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SuccessMessage {
    // MOIM
    CREATE_MOIM_SUCCESS("모임 생성에 성공했습니다."),
    DELETE_MOIM_SUCCESS("모임 삭제에 성공했습니다.");
    private final String message;
}
