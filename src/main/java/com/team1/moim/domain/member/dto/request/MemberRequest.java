package com.team1.moim.domain.member.dto.request;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MemberRequest {
    @NotEmpty(message = "이메일은 필수입니다.")
    private String email;

    @NotEmpty(message = "닉네임은 필수입니다.")
    private String nickname;

    public static Member toEntity(String email, String nickname) {
        return Member.builder()
                .email(email)
                .nickname(nickname)
                .build();
    }
}
