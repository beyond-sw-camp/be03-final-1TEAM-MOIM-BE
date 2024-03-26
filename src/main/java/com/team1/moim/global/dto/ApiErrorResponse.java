package com.team1.moim.global.dto;

import com.team1.moim.global.exception.ErrorCode;
import com.team1.moim.global.exception.ErrorDetail;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ApiErrorResponse extends ApiResponse{

    private final ErrorDetail error;

    private ApiErrorResponse(HttpStatus status, String path, String type, ErrorCode errorCode){
        super(false, status, path);
        this.error = ErrorDetail.of(type, errorCode);
    }

    public static ApiErrorResponse of(HttpStatus status, String path, String type, ErrorCode errorCode){
        return new ApiErrorResponse(status, path, type, errorCode);
    }
}
