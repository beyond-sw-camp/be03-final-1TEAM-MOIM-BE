package com.team1.moim.global.config.sse.repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Repository
public class EmitterRepository {
    // 스레드 safe한 자료구조를 사용.
    private final Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();

    public void save(String email, SseEmitter emitter) {
        emitters.put(email, emitter);
    }

    public Optional<SseEmitter> findByEmail(String email) {
        return Optional.ofNullable(emitters.get(email));
    }

    public void deleteByEmail(String email) {
        emitters.remove(email);
    }

    public SseEmitter get(String email) {
        return emitters.get(email);
    }

    public int getEmitterSize(){
        return emitters.size();
    }

    public boolean containKey(String email){
        return emitters.containsKey(email);
    }
}
