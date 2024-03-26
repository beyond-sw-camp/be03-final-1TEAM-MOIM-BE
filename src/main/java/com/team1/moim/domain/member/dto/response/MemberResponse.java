package com.team1.moim.domain.member.dto.response;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.entity.Role;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberResponse {
    private Long id;
    private String email;
    private String nickname;
    private String profileImage;
    private Role role;
    private String deleteYn;

    public static MemberResponse from(Member member){
        return MemberResponse.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .profileImage(member.getProfileImage())
                .role(member.getRole())
                .deleteYn(member.getDeleteYn())
                .build();
    }
}
