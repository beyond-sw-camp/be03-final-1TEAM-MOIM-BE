package com.team1.moim.domain.auth.dto.request;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SignUpRequest {
    @Email(message = "이메일 형식에 맞게 입력해주세요.")
    @NotEmpty(message = "이메일이 비어있으면 안됩니다.")
    private String email;

    @NotEmpty(message = "비밀번호가 비어있으면 안됩니다.")
    private String password;

    @NotEmpty(message = "닉네임이 비어있으면 안됩니다.")
    private String nickname;

    private MultipartFile profileImage;

    public Member toEntity(PasswordEncoder passwordEncoder, Role role, String imageUrl){
        String finalPassword = null;
        if (password != null){
            finalPassword = passwordEncoder.encode(password);
        }

        return Member.builder()
                .email(email)
                .password(finalPassword)
                .nickname(nickname)
                .profileImage(imageUrl)
                .role(role)
                .socialType(null)
                .socialId(null)
                .build();
    }
}
