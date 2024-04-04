//package com.team1.moim.domain.group.dto.request;
//
//import com.team1.moim.domain.group.entity.Group;
//import com.team1.moim.domain.group.entity.GroupInfo;
//import com.team1.moim.domain.member.entity.Member;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class GroupVotedRequest {
//    private String isAgreed;
//
//    public GroupInfo toEntity(Group group, Member member, String isAgreed) {
//        return GroupInfo.builder()
//                .group(group)
//                .isAgreed(isAgreed)
//                .member(member)
//                .build();
//    }
//}
