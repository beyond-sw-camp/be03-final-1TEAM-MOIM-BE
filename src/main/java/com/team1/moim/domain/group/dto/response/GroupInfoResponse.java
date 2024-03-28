package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.GroupInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupInfoResponse {
    private Long id;
    private String email;
    private String nickname;
    private String isVoted;
    private String isAgreed;

    public static GroupInfoResponse from(GroupInfo groupInfo) {
        return GroupInfoResponse.builder()
                .id(groupInfo.getId())
                .email(groupInfo.getMember().getEmail())
                .nickname(groupInfo.getMember().getNickname())
                .isVoted(groupInfo.getIsVoted())
                .isAgreed(groupInfo.getIsAgreed())
                .build();
    }
}
