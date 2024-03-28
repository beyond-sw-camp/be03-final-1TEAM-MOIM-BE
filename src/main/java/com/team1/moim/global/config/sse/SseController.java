package com.team1.moim.global.config.sse;

import com.team1.moim.domain.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;
import java.util.stream.Collectors;

@RestController
public class SseController {

    private final SseService sseService;
    private final MemberRepository memberRepository;

    @Autowired
    public SseController(SseService sseService, MemberRepository memberRepository) {
        this.sseService = sseService;
        this.memberRepository = memberRepository;
    }

    @GetMapping(value = "/connect", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> connect() throws ServiceUnavailableException {
        // 1. SSE 연결하기
        SseEmitter emitter = new SseEmitter(60 * 1000L);//만료시간 설정 30초
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
//        sseService.add(authentication.getName(),emitter);
        String email = "bbb";
        sseService.add(email, emitter);
        try {
            // 최초 연결시 메시지를 안 보내면 503 Service Unavailable 에러 발생
            emitter.send(SseEmitter.event().name("connect").data("connected!"));
        } catch (IOException e) {
            throw new ServiceUnavailableException();
        }
        return ResponseEntity.ok(emitter);

    }
}
