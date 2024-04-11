package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VoteResponse {
    private Long groupInfoId; // 모임 정보 id
    private String participantName; // 참여자 닉네임
    private String isAgree; // 찬성 또는 반대

    public static VoteResponse from(GroupInfo groupInfo, Member participant){
        return VoteResponse.builder()
                .groupInfoId(groupInfo.getId())
                .participantName(participant.getNickname())
                .isAgree(groupInfo.getIsAgreed())
                .build();
    }
}
