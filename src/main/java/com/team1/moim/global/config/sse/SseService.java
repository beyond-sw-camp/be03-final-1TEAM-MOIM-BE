package com.team1.moim.global.config.sse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class SseService {
    private final EmitterRepository emitterRepository;

    @Autowired
    public SseService(EmitterRepository emitterRepository) {
        this.emitterRepository = emitterRepository;
    }

    SseEmitter add(String email,SseEmitter emitter){
        // 현재 저장된 emitter의 수를 조회하여 자동 삭제를 확인
        //System.out.println(emitterRepository.getEmitterSize());
        emitterRepository.save(email,emitter);
        /*
        Register code to invoke when the async request completes.
        This method is called from a container thread when an async request completed for any reason including timeout and network error.
        This method is useful for detecting that a ResponseBodyEmitter instance is no longer usable.
        */
        emitter.onCompletion(()->{
            // 만일 emitter가 만료되면 삭제한다.
            // System.out.println(email);
            emitterRepository.deleteByEmail(email);
        });
        /*
        Register code to invoke when the async request times out. This method is called from a container thread when an async request times out.
         */
        emitter.onTimeout(()->{
            emitterRepository.get(email).complete();
        });
        return emitter;
    }
}
