package com.team1.moim.global.config.sse.controller;

import com.team1.moim.global.config.sse.service.SseService;
import javax.naming.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@Slf4j
public class SseController {

    private final SseService sseService;

    @Autowired
    public SseController(SseService sseService) {
        this.sseService = sseService;
    }

//    SSE 연결
    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect() throws ServiceUnavailableException {
        log.info("sse 연결");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(sseService.add(email));
    }
}
