package com.team1.moim.domain.member.service;

import com.team1.moim.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class MemberService {

    private static final String FILE_TYPE = "members";

    private final MemberRepository memberRepository;

}
