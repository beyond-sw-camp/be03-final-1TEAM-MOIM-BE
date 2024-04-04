package com.team1.moim.global.config.sse.dto;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupAlarm;
import com.team1.moim.domain.group.entity.GroupInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class GroupNotification {
    private Long groupId; // 모임 ID
    private String hostName; // 호스트명
    private String message; // 참여자에게 보여지는 메시지
    private String groupTitle; // 모임명
    private LocalDateTime voteDeadline; // 투표 마감 기한

    public static GroupNotification from(Group group,
                                         String message) {

        return GroupNotification.builder()
                .groupId(group.getId())
                .hostName(group.getMember().getNickname())
                .message(message)
                .groupTitle(group.getTitle())
                .voteDeadline(group.getVoteDeadline())
                .build();
    }
}
