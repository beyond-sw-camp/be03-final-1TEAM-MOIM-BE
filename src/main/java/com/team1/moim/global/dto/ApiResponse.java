package com.team1.moim.global.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * ResponseEntity의 body로 들어갈 부분을 정의할 곳
 * 응답이 문제 없을 때는 data가, 예외가 발생했을 때는 error가 보이게 함
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class ApiResponse {

    private final Boolean success;
    private final HttpStatus status;
    private final String path;

    public static ApiResponse of(Boolean success, HttpStatus status, String path){
        return new ApiResponse(success, status, path);
    }
}
