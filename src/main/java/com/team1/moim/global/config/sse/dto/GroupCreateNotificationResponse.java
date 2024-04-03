package com.team1.moim.global.config.sse.dto;

import com.team1.moim.domain.group.entity.GroupAlarm;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupCreateNotificationResponse {
    private String hostname;                // 호스트명
    private String message;                 // 알림 메시지
    private String title;                   // 모임명

    public static GroupCreateNotificationResponse from(GroupAlarm groupAlarm) {

        String hostName = groupAlarm.getGroup().getMember().getNickname();
        String title = groupAlarm.getGroup().getTitle();

        String message = MessageCreator.createMessage(groupAlarm);

        return GroupCreateNotificationResponse.builder()
                .hostname(hostName)
                .message(message)
                .title(title)
                .build();
    }
}
