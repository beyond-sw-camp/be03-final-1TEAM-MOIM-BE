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

    //    일정ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    //    설정시간
    @Column(nullable = false)
    private int setTime;

    //    알림타입
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Alarmtype alarmtype;

    @Builder
    public Alarm(Alarmtype alarmtype, int setTime, Event event) {
        this.event = event;
        this.alarmtype = alarmtype;
        this.setTime = setTime;
    }
}
