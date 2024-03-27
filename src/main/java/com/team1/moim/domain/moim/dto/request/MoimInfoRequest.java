package com.team1.moim.domain.moim.dto.request;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class MoimInfoRequest {

    private String memberEmail;
//    private String isVoted;
//    private String isAgreed;

    public static MoimInfo toEntity(Moim moim, Member member) {
        return MoimInfo.builder()
                .moim(moim)
                .member(member)
                .build();
    }
}
