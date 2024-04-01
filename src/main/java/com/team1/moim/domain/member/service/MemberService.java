package com.team1.moim.domain.member.service;

import com.team1.moim.domain.member.dto.response.MemberResponse;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private static final String FILE_TYPE = "members";

    private final MemberRepository memberRepository;

    @Transactional
    public String delete(){
        Member findMember = findMember();
        findMember.withdraw();

        return findMember.getEmail() + " 회원을 삭제하였습니다.";
    }

    @Transactional
    public MemberResponse view() {

        Member findMember = findMember();

        return MemberResponse.from(findMember);
    }

    private Member findMember(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);
    }


}
