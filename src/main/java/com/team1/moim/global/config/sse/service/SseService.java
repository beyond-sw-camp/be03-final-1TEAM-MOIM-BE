package com.team1.moim.global.config.sse.service;

import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.sse.dto.response.NotificationResponse;
import com.team1.moim.global.config.sse.repository.EmitterRepository;
import java.io.IOException;
import javax.naming.ServiceUnavailableException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
@Slf4j
public class SseService {

    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1시간
    private final EmitterRepository emitterRepository;

    @Autowired
    public SseService(EmitterRepository emitterRepository, MemberRepository memberRepository) {
        this.emitterRepository = emitterRepository;
    }

    public SseEmitter add(String email) throws ServiceUnavailableException {
        /**
         Emitter는 발신기라는 뜻
         SSE 연결을 위해서 유효 시간이 담긴 SseEmitter 객체를 만들어 반환해야 한다.
         */
        SseEmitter emitter = new SseEmitter(TIMEOUT); // 만료시간 설정
        // 현재 저장된 emitter의 수를 조회하여 자동 삭제를 확인
//        System.out.println(emitterRepository.getEmitterSize());
        emitterRepository.save(email, emitter);
        /*
        Register code to invoke when the async request completes.
        This method is called from a container thread when an async request completed for any reason including timeout and network error.
        This method is useful for detecting that a ResponseBodyEmitter instance is no longer usable.
        */
        emitter.onCompletion(() -> {
            // 만일 emitter가 만료되면 삭제한다.
            System.out.println(email);
            emitterRepository.deleteByEmail(email);
        });
        /*
        Register code to invoke when the async request times out. This method is called from a container thread when an async request times out.
         */
        emitter.onTimeout(() -> {
            emitterRepository.get(email).complete();
        });
        try {
            // 최초 연결시 메시지를 안 보내면 503 Service Unavailable 에러 발생
            emitter.send(SseEmitter.event().name("connect").data(email + " connected!"));
        } catch (IOException e) {
            throw new ServiceUnavailableException();
        }
        return emitter;
    }

    public void sendEventAlarm(String email, NotificationResponse notificationResponse) {
        try {
            emitterRepository.get(email).send(SseEmitter.event()
                    .name("sendEventAlarm")
                    .data(notificationResponse));
        } catch (IOException e) {
//            emitterRepository.deleteById(emitterId);
            throw new RuntimeException(e);
        }
    }

    boolean containKey(String email) {
        return emitterRepository.containKey(email);
    }

    public void send(Object data, String email, SseEmitter sseEmitter) {
        try {
            log.info("send to client {}:[{}]", email, data);
            sseEmitter.send(SseEmitter.event().id(email).data(data, MediaType.APPLICATION_JSON));
        } catch (IOException | IllegalStateException e) {
            // 만료된 emitter로 send() 메서드를 호출하면 IllegalStateException 발생
            // 에러 발생 시 해당 emitter를 삭제
            log.error("IOException or IllegalStateException is occured. ", e);
            emitterRepository.deleteByEmail(email);
        }
    }
}
