package com.team1.moim.domain.event.entity;

import com.team1.moim.global.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Alarm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    일정 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    //    설정시간
    @Column(nullable = false)
    private int setTime;

    //    알림타입
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlarmType alarmtype;

    //    알림전송여부
    @Column(nullable = false)
    private String sendYn = "N";

    @Builder
    public Alarm(AlarmType alarmtype, int setTime, Event event) {
        this.event = event;
        this.alarmtype = alarmtype;
        this.setTime = setTime;
    }

    public void attachEvent(Event event) {
        this.event = event;
        event.getAlarms().add(this);
    }

//    알림 전송 체크
    public void sendCheck(String sendYn) {
        this.sendYn = sendYn;
    }
}
