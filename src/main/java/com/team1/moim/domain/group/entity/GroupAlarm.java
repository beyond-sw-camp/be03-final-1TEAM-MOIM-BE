package com.team1.moim.domain.group.entity;

import com.team1.moim.domain.event.entity.AlarmType;
import com.team1.moim.global.config.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupAlarm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 모임 ID
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    // 알림타입
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private AlarmType alarmType;

    // 알림 전송 여부
    @Column(nullable = false)
    private String sendYn = "N";

    // 모임 확정 시간 임박까지 N분 전 알림 전송
    @Column(nullable = false)
    private int deadlineAlarm;

    @Builder
    public GroupAlarm(Group group, AlarmType alarmType, int deadlineAlarm) {
        this.group = group;
        this.alarmType = alarmType;
        this.deadlineAlarm = deadlineAlarm;
    }

    // 알림 전송 체크
    public void sendCheck(String sendYn) {
        this.sendYn = sendYn;
    }
}
