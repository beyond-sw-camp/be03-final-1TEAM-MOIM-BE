package com.team1.moim.global.config.sse.dto;

import com.team1.moim.domain.group.entity.GroupAlarm;
import com.team1.moim.domain.group.entity.GroupInfo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GroupNotification {
    private Long groupId; // 모임 ID
    private Long groupInfoId; // 모임 정보 ID
    private String hostName; // 호스트명
    private String message; // 참여자에게 보여지는 메시지
    private String groupTitle; // 모임명
    private LocalDateTime voteDeadline; // 투표 마감 기한

    public static GroupNotification from(GroupInfo groupInfo, String message) {

        return GroupNotification.builder()
                .groupId(groupInfo.getGroup().getId())
                .groupInfoId(groupInfo.getId())
                .hostName(groupInfo.getGroup().getMember().getNickname())
                .message(message)
                .groupTitle(groupInfo.getGroup().getTitle())
                .voteDeadline(groupInfo.getGroup().getVoteDeadline())
                .build();
    }
}
