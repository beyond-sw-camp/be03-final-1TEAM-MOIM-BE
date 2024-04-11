package com.team1.moim.domain.notification.dto;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.notification.NotificationType;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class NotificationResponseNew {
    private NotificationType notificationType;
    // event 또는 gruop의 id
    private Long id;
    private String nickname;
    private String message;
    private String sendTime;
    private String readYn;

    @Builder
    public NotificationResponseNew(Long id, String nickname, String message, String sendTime, NotificationType notificationType, String readYn) {
        this.id = id;
        this.nickname = nickname;
        this.message = message;
        this.sendTime = sendTime;
        this.notificationType = notificationType;
        this.readYn = readYn;
    }

    public static NotificationResponseNew fromEvent(NotificationResponse notificationResponse){
        return NotificationResponseNew.builder()
                .id(notificationResponse.getEventId())
                .nickname(notificationResponse.getNickname())
                .message(notificationResponse.getMessage())
                .sendTime(notificationResponse.getSendTime())
                .notificationType(notificationResponse.getNotificationType())
                .readYn(notificationResponse.getReadYn())
                .build();
    }

    public static NotificationResponseNew fromGroup(GroupNotification groupNotification){
        return NotificationResponseNew.builder()
                .id(groupNotification.getGroupId())
                .nickname(groupNotification.getHostName())
                .message(groupNotification.getMessage())
                .sendTime(groupNotification.getSendTime())
                .notificationType(groupNotification.getNotificationType())
                .readYn(groupNotification.getReadYn())
                .build();
    }

    public void read(String readYn) {
        this.readYn = readYn;
    }


}
