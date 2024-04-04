package com.team1.moim.domain.group.dto.request;

import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.member.entity.Member;
import lombok.Data;

@Data
public class GroupInfoRequest {

    private String memberEmail;

    public GroupInfo toEntity(Member member) {
        return GroupInfo.builder()
                .member(member)
                .build();
    }

}
