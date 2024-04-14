package com.team1.moim.domain.notification.dto;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.notification.NotificationType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Getter
public class GroupNotification {
    private Long alarmId;
    private Long groupId; // 모임 ID
    private String hostName; // 호스트명
    private String message; // 참여자에게 보여지는 메시지
    private String groupTitle; // 모임명
    private String voteDeadline; // 투표 마감 기한
    private String sendTime;
    private NotificationType notificationType;
    private String readYn = "N";

    @Builder
    public GroupNotification(Long alarmId, Long groupId, String hostName, String message, String groupTitle, String voteDeadline, String sendTime, NotificationType notificationType) {
        this.alarmId = alarmId;
        this.groupId = groupId;
        this.hostName = hostName;
        this.message = message;
        this.groupTitle = groupTitle;
        this.voteDeadline = voteDeadline;
        this.sendTime = sendTime;
        this.notificationType = notificationType;
    }
    public static GroupNotification from(Group group,
                                         String message,NotificationType notificationType, LocalDateTime sendTime) {

        return GroupNotification.builder()
                .groupId(group.getId())
                .hostName(group.getMember().getNickname())
                .message(message)
                .groupTitle(group.getTitle())
                .voteDeadline(group.getVoteDeadline().toString())
                .sendTime(sendTime.toString())
                .notificationType(notificationType)
                .build();
    }
}
