package com.team1.moim.global.exception;

import com.team1.moim.global.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(annotations = RestController.class)
@Slf4j
public class GlobalExceptionHandler {

    private static final String LOG_FORMAT = "Class: {}, Status: {}, Message: {}";

    @ExceptionHandler(MoimException.class)
    public ResponseEntity<ApiErrorResponse> handleMoimException(HttpServletRequest request, MoimException e){

        log.error(LOG_FORMAT, e.getClass().getSimpleName(), e.getErrorCode(), e.getErrorCode().getMessage());
        ErrorCode errorCode = e.getErrorCode();
        HttpStatus status = errorCode.getStatus();

        return ResponseEntity
                .status(status)
                .body(ApiErrorResponse.of(
                        status,
                        request.getServletPath(),
                        e.getClass().getSimpleName(),
                        errorCode
                ));
    }
}
