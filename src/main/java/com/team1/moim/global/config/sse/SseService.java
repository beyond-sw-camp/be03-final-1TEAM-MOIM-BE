package com.team1.moim.global.config.sse;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;

@Component
@Slf4j
public class SseService {

    private static final Long TIMEOUT = 60 * 60 * 1000L; // 1시간
    private final EmitterRepository emitterRepository;

    @Autowired
    public SseService(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    SseEmitter add(String email) throws ServiceUnavailableException {

        //  SSE 연결을 위해서 만료 시간이 담긴 SseEmitter 객체를 만들어 반환해야 함
        SseEmitter emitter = new SseEmitter(TIMEOUT); // 만료 시간 설정

        // 현재 저장된 emitter의 수를 조회하여 자동 삭제를 확인
//        log.info("emitter size: " + emitterRepository.getEmitterSize());
        emitterRepository.save(email,emitter);

        emitter.onCompletion(()->{
            // 만일 emitter가 만료되면 삭제
             System.out.println(email);
            emitterRepository.deleteByEmail(email);
        });

        emitter.onTimeout(()->{
            emitterRepository.get(email).complete();
        });
        try {
            // 최초 연결시 메시지를 안 보내면 503 Service Unavailable 에러 발생
            emitter.send(SseEmitter.event().name("connect").data(email + " connected!"));
        }catch (IOException e){
            throw new ServiceUnavailableException();
        }
        return emitter;
    }

    public void sendEventAlarm(String email, NotificationResponse notificationResponse){
        try {
            emitterRepository.get(email).send(SseEmitter.event()
                    .name("sendEventAlarm")
                    .data(notificationResponse));
        } catch (IOException e){
//            emitterRepository.deleteById(emitterId);
            throw new RuntimeException(e);
        }
    }

    boolean containKey(String email){
        return emitterRepository.containKey(email);
    }


}
