package com.team1.moim.global.config.security.oauth2;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.entity.Role;
import com.team1.moim.domain.member.entity.SocialType;
import com.team1.moim.global.config.security.oauth2.userinfo.GoogleOAuth2UserInfo;
import com.team1.moim.global.config.security.oauth2.userinfo.KakaoOAuth2UserInfo;
import com.team1.moim.global.config.security.oauth2.userinfo.OAuth2UserInfo;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

/**
 * 각 소셜에서 받아오는 데이터가 다르므로,
 * 소셜 별로 데이터를 받은 데이터를 분기 처리하는 DTO 클래스
 */

@Getter
public class OAuthAttributes {

    private String nameAttributeKey; // OAuth2 로그인 진행 시 키가 되는 필드 값, PK와 동일
    private OAuth2UserInfo oAuth2UserInfo; // 소셜 타입별 로그인 유저 정보 (nickname, email, profile_image 등)

    @Builder
    private OAuthAttributes(String nameAttributeKey, OAuth2UserInfo oauth2UserInfo){
        this.nameAttributeKey = nameAttributeKey;
        this.oAuth2UserInfo = oauth2UserInfo;
    }

    /**
     * SocialType에 맞는 메소드 호출하여 OAuthAttributes 객체 반환
     * @param userNameAttributeName: OAuth2 로그인 시 키(PK)가 되는 값
     * @param attributes: OAuth 서비스의 유저 정보들
     */
    public static OAuthAttributes of(SocialType socialType,
                                     String userNameAttributeName,
                                     Map<String, Object> attributes){

        if (socialType == SocialType.GOOGLE) {
            return ofGoogle(userNameAttributeName, attributes);
        }

        return ofKakao(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName,
                                            Map<String, Object> attributes){
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new GoogleOAuth2UserInfo(attributes))
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName,
                                            Map<String, Object> attributes){
        return OAuthAttributes.builder()
                .nameAttributeKey(userNameAttributeName)
                .oauth2UserInfo(new KakaoOAuth2UserInfo(attributes))
                .build();
    }

    // of메소드로 OAuthAttributes 객체가 생성되어, 유저 정보들이 담긴 OAuth2UserInfo가 소셜 타입별로 주입된 상태
    public Member toEntity(SocialType socialType, OAuth2UserInfo oAuth2UserInfo){
        return Member.builder()
                .socialType(socialType)
                .socialId(oAuth2UserInfo.getId())
                // email은 JWT Token을 발급하기 위한 용도뿐이므로 UUID를 사용하여 임의로 설정
                .email(UUID.randomUUID() + "@socialMember.com")
                .nickname(oAuth2UserInfo.getNickname())
                .profileImage(oAuth2UserInfo.getImageUrl())
                .role(Role.USER)
                .build();
    }

}
