package com.team1.moim.global.config.sse.dto;

import com.team1.moim.domain.group.entity.GroupAlarm;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupScheduledNotificationResponse {
    private String hostname;                // 호스트명
    private String message;                 // 알림 메시지
    private String title;                   // 모임명
    private LocalDateTime voteDeadline;     // 마감시간

    public static GroupScheduledNotificationResponse from(GroupAlarm groupAlarm) {

        String hostName = groupAlarm.getGroup().getMember().getNickname();
        String title = groupAlarm.getGroup().getTitle();
        LocalDateTime voteDeadline = groupAlarm.getGroup().getVoteDeadline();

        String message = MessageCreator.createMessage(groupAlarm);

        return GroupScheduledNotificationResponse.builder()
                .hostname(hostName)
                .message(message)
                .title(title)
                .voteDeadline(voteDeadline)
                .build();
    }
}
