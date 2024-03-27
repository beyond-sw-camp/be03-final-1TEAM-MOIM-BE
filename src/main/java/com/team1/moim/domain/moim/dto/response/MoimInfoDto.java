package com.team1.moim.domain.moim.dto.response;

import lombok.Data;

@Data
public class MoimInfoDto {
    private String memberEmail;
    private String isVoted;
    private String isAgreed;

    public MoimInfoDto(String memberEmail, String isVoted, String isAgreed) {
        this.memberEmail = memberEmail;
        this.isVoted = isVoted;
        this.isAgreed = isAgreed;
    }
}
