package com.team1.moim.domain.event.controller;

import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // 일정 등록
    @PostMapping
    public ResponseEntity<EventResponse> create(@Valid EventRequest request){
        return ResponseEntity.ok().body(eventService.create(request));
    }

    // 일정 수정
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponse> update(
            @PathVariable(name = "eventId") Long eventId, @Valid EventRequest request) {
        return ResponseEntity.ok().body(eventService.update(eventId, request));
    }



}
