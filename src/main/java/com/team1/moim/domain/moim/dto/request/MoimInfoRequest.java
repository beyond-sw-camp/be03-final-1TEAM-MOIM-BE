package com.team1.moim.domain.moim.dto.request;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import lombok.Data;

@Data
public class MoimInfoRequest {

    private String isVoted;
    private String isAgreed;

    public static MoimInfo toEntity(Moim moim, Member member, String isVoted, String isAgreed) {
        return MoimInfo.builder()
                .moim(moim)
                .member(member)
                .isVoted(isVoted)
                .isAgreed(isAgreed)
                .build();
    }
}
