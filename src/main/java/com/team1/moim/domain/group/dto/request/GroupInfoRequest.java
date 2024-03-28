package com.team1.moim.domain.group.dto.request;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupInfo;
import lombok.Data;

@Data
public class GroupInfoRequest {

    private String memberEmail;

    public static GroupInfo toEntity(Group group, Member member) {
        return GroupInfo.builder()
                .group(group)
                .member(member)
                .build();
    }
}
