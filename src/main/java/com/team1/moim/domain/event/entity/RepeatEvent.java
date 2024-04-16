package com.team1.moim.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "repeatValue")
public class RepeatEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    EventID
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    //    반복타입
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private RepeatType repeatType;

    // 반복 종료일
    @Column(nullable = false)
    private LocalDate repeatEndDate;

    @Builder
    public RepeatEvent(Event event, RepeatType repeatType, LocalDate repeatEndDate) {
        this.event = event;
        this.repeatType = repeatType;
        this.repeatEndDate = repeatEndDate;
    }

    //일정 종료일 변경
    public void changeEndDate(LocalDate repeatEndDate) {
        this.repeatEndDate = repeatEndDate;
    }
}
