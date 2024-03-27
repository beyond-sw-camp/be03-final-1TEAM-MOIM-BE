package com.team1.moim.domain.auth.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmailResponse {
    private String email;
    private String authCode;

    public static EmailResponse from(String email, String authCode){
        return EmailResponse.builder()
                .email(email)
                .authCode(authCode)
                .build();
    }
}
