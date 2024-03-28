package com.team1.moim.global.config.sse;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.EmailNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.naming.ServiceUnavailableException;
import java.io.IOException;

@Component
@Slf4j
public class SseService {
    private final EmitterRepository emitterRepository;
    private final MemberRepository memberRepository;

    @Autowired
    public SseService(EmitterRepository emitterRepository, MemberRepository memberRepository) {
        this.emitterRepository = emitterRepository;
        this.memberRepository = memberRepository;
    }

    SseEmitter add(String email,SseEmitter emitter) throws ServiceUnavailableException {
        // 현재 저장된 emitter의 수를 조회하여 자동 삭제를 확인
//        System.out.println(emitterRepository.getEmitterSize());
        emitterRepository.save(email,emitter);
        /*
        Register code to invoke when the async request completes.
        This method is called from a container thread when an async request completed for any reason including timeout and network error.
        This method is useful for detecting that a ResponseBodyEmitter instance is no longer usable.
        */
        emitter.onCompletion(()->{
            // 만일 emitter가 만료되면 삭제한다.
             System.out.println(email);
            emitterRepository.deleteByEmail(email);
        });
        /*
        Register code to invoke when the async request times out. This method is called from a container thread when an async request times out.
         */
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

    SseEmitter get(String email){
        return emitterRepository.get(email);
    }

    boolean containKey(String email){
        return emitterRepository.containKey(email);
    }


}
