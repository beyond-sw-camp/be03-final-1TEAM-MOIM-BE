package com.team1.moim.domain.member.entity;

import lombok.Getter;

@Getter
public enum Role {
    // 스프링 시큐리티에서는 권한(Role) 코드에 항상 "ROLE_" 접두사가 앞에 붙어야 함
    USER, ADMIN
}
