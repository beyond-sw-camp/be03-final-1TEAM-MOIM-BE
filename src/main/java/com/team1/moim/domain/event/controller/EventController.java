package com.team1.moim.domain.event.controller;

import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.ToDoList;
import com.team1.moim.domain.event.service.EventService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<EventResponse> create(@Valid EventRequest request, @RequestPart(value = "toDoListRequests", required = false) List<ToDoListRequest> toDoListRequests ){
        return ResponseEntity.ok().body(eventService.create(request, toDoListRequests));
    }

    // 일정 수정
    @PatchMapping("/{eventId}")
    public ResponseEntity<EventResponse> update(
            @PathVariable(name = "eventId") Long eventId, @Valid EventRequest request) {
        return ResponseEntity.ok().body(eventService.update(eventId, request));
    }

    // 일정 삭제
    @DeleteMapping("/{eventId}")
    public ResponseEntity<String> delete(
            @PathVariable(name = "eventId") Long eventId) {
        eventService.delete(eventId);
        return ResponseEntity.ok("삭제되었습니다.");
    }



}
