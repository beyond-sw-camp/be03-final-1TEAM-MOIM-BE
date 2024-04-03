package com.team1.moim.domain.group.entity;

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

    // 알림 종류
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupAlarmType groupAlarmType;

    // 알림 시간 타입
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private GroupAlarmTimeType groupAlarmTimeType;

    // 알림 전송 여부
    @Column(nullable = false)
    private String sendYn = "N";

    // 모임 확정 시간 임박 알림에 지정할 시간
    // GroupAlarm의 타입이 MOIM_DEADLINE인 경우에만 채워진다.
    private int deadlineAlarm;

    @Builder
    public GroupAlarm(
            Group group,
            GroupAlarmType groupAlarmType,
            GroupAlarmTimeType groupAlarmTimeType,
            int deadlineAlarm) {
        this.group = group;
        this.groupAlarmType = groupAlarmType;
        this.groupAlarmTimeType = groupAlarmTimeType;
        this.deadlineAlarm = deadlineAlarm;
    }

    // 알림 전송 체크
    public void sendCheck(String sendYn) {
        this.sendYn = sendYn;
    }
}
