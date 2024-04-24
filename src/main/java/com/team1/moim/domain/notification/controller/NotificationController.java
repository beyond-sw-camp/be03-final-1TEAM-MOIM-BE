package com.team1.moim.domain.notification.controller;

import com.team1.moim.domain.notification.dto.NotificationResponseNew;
import com.team1.moim.domain.notification.service.NotificationService;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //    알림 목록 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<NotificationResponseNew>>> getAlarms(HttpServletRequest httpServletRequest) {

        log.info("알림 목록 조회");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        notificationService.getAlarms()));
    }

//    //    알림 읽음으로 변경
//    @PreAuthorize("hasRole('ROLE_USER')")
//    @PatchMapping("/{memberId}/{alarmId}")
//    public ResponseEntity<ApiSuccessResponse<String>> readAlarm(HttpServletRequest httpServletRequest,
//                                                                @PathVariable(name = "memberId") Long memberId,
//                                                                @PathVariable(name = "alarmId") Long alarmId) {
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(ApiSuccessResponse.of(
//                        HttpStatus.OK,
//                        httpServletRequest.getServletPath(),
//                        notificationService.readAlarm(memberId, alarmId)));
//    }
}
