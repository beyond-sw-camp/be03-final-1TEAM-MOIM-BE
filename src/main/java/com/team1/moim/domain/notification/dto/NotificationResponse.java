package com.team1.moim.domain.notification.dto;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;

import com.team1.moim.domain.notification.NotificationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class NotificationResponse {
    @Id
    private Long alarmId;
    private String nickname;
    private String message;
    private String sendTime;
    private NotificationType notificationType;
    private String readYn = "N";

    @Builder
    public NotificationResponse(Long alarmId, String nickname, String message, String sendTime, NotificationType notificationType) {
        this.alarmId = alarmId;
        this.nickname = nickname;
        this.message = message;
        this.sendTime = sendTime;
        this.notificationType = notificationType;
    }


    public static NotificationResponse from(Alarm alarm, Member member, LocalDateTime sendTime, NotificationType notificationType){
        String message = alarm.getSetTime() + alarm.getAlarmtype().toString()+"전 알람입니다.";
        return NotificationResponse.builder()
                .alarmId(alarm.getId())
                .nickname(member.getNickname())
                .message(message)
                .sendTime(sendTime.toString())
                .notificationType(notificationType)
                .build();
    }

    public void read(String readYn) {
        this.readYn = readYn;
    }


}
