package com.team1.moim.domain.notification.dto;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.event.entity.AlarmType;
import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.group.entity.GroupAlarmTimeType;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;

import com.team1.moim.domain.notification.NotificationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class EventNotification {
    @Id
    private Long alarmId;
    private Long eventId;
    private String nickname;
    private String message;
    private String sendTime;
    private NotificationType notificationType;
    private String readYn = "N";

    @Builder
    public EventNotification(Long alarmId, Long eventId, String nickname, String message, String sendTime, NotificationType notificationType) {
        this.alarmId = alarmId;
        this.eventId = eventId;
        this.nickname = nickname;
        this.message = message;
        this.sendTime = sendTime;
        this.notificationType = notificationType;
    }

    public static EventNotification from(Event event , Alarm alarm, Member member, LocalDateTime sendTime, NotificationType notificationType){
        String alarmType = "";
        if(alarm.getAlarmtype() == AlarmType.D) {
            alarmType = "일";
        }if(alarm.getAlarmtype() == AlarmType.H) {
            alarmType = "시간";
        }if(alarm.getAlarmtype() == AlarmType.M) {
            alarmType = "분";
        }
        String message = '"' + event.getTitle() + '"' + " " + alarm.getSetTime() + alarmType +" 전 알람입니다.";
        return EventNotification.builder()
                .alarmId(alarm.getId())
                .eventId(event.getId())
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
