package com.team1.moim.domain.notification.dto;

import com.team1.moim.domain.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    public static NotificationResponseNew fromEvent(EventNotification eventNotification){
        return NotificationResponseNew.builder()
                .id(eventNotification.getEventId())
                .nickname(eventNotification.getNickname())
                .message(eventNotification.getMessage())
                .sendTime(eventNotification.getSendTime())
                .notificationType(eventNotification.getNotificationType())
                .readYn(eventNotification.getReadYn())
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
