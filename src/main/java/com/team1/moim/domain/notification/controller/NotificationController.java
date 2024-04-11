package com.team1.moim.domain.notification.controller;

import com.team1.moim.domain.event.dto.request.AlarmRequest;
import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.RepeatRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.Matrix;
import com.team1.moim.domain.event.service.EventService;
import com.team1.moim.domain.event.service.PublicHoliyDayAPI;
import com.team1.moim.domain.notification.service.NotificationService;
import com.team1.moim.global.config.sse.dto.NotificationResponse;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Autowired
    public NotificationController(EventService eventService, NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    //    알림 목록 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{memberId}")
    public ResponseEntity<ApiSuccessResponse<List<NotificationResponse>>> getAlarms(HttpServletRequest httpServletRequest,
                                                                                    @PathVariable("memberId") Long memberId) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        notificationService.getAlarms(memberId)));
    }

    //    알림 읽음으로 변경
    @PreAuthorize("hasRole('ROLE_USER')")
    @PatchMapping("/{memberId}/{alarmId}")
    public ResponseEntity<ApiSuccessResponse<String>> readAlarm(HttpServletRequest httpServletRequest,
                                                                @PathVariable(name = "memberId") Long memberId,
                                                                @PathVariable(name = "alarmId") Long alarmId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        notificationService.readAlarm(memberId, alarmId)));
    }
}
