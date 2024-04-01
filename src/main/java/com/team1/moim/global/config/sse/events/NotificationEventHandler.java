package com.team1.moim.global.config.sse.events;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class NotificationEventHandler {
    /**
     * 알림이 필요한 작업이 완료되는 시점에 알림 이벤트를 발행하고,
     * 이벤트 리스너는 이벤트가 발행되었을 때 정의된 메서드를 실행한다.
     */

    // 이벤트 발행을 처리하는 인터페이스
    private final ApplicationEventPublisher eventPublisher;

    // publishEvent()로 이벤트를 발행
//    public void publishEvent(NotificationEvent event) {
//        eventPublisher.publishEvent(event);
//    }
}
