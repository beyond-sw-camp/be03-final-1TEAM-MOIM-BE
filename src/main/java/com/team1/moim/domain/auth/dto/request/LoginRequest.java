package com.team1.moim.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginRequest {
    @Email(message = "이메일 형식에 맞게 입력해주세요.")
    @NotEmpty(message = "이메일이 비어있으면 안됩니다.")
    private String email;

    @NotEmpty(message = "비밀번호가 비어있으면 안됩니다.")
    private String password;
}
