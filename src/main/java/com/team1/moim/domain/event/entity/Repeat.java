package com.team1.moim.domain.event.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "repeatValue")
public class Repeat {

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
    private Repeat_type repeatType;

    // 반복 종료일
    @Column(nullable = false)
    private LocalDate reapet_end_date;


    @Builder
    public Repeat(Event event, Repeat_type repeatType, LocalDate reapet_end_date) {
        this.event = event;
        this.repeatType = repeatType;
        this.reapet_end_date = reapet_end_date;
    }

    //일정 종료일 변경
    public void changeEndDate(LocalDate repeat_end_date) {
        this.reapet_end_date = repeat_end_date;
    }


}
