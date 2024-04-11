package com.team1.moim.domain.notification.service;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.exception.MemberNotMatchException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.domain.notification.dto.NotificationResponseNew;
import com.team1.moim.domain.notification.exception.NotificationNotFoundException;
import com.team1.moim.global.config.redis.RedisService;
import com.team1.moim.domain.notification.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final MemberRepository memberRepository;
    private final RedisService redisService;

    public List<NotificationResponseNew> getAlarms(Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        String key = findMember.getEmail();
        String loginEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!key.equals(loginEmail)) throw new MemberNotMatchException();
        List<NotificationResponseNew> alarms= redisService.getList(key);
        if(alarms.isEmpty()) throw new NotificationNotFoundException();
        return alarms;
    }

//    public String readAlarm(Long memberId, Long alarmId) {
//        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
//        log.info(member.getNickname() + "회원");
//        String key = member.getEmail();
//        String loginEmail = SecurityContextHolder.getContext().getAuthentication().getName();
//        if(!key.equals(loginEmail)) throw new MemberNotMatchException();
//        List<NotificationResponseNew> alarms= redisService.getList(key);
//        alarms.stream()
//                .filter(notification -> Objects.equals(notification.getAlarmId(), alarmId))
//                .findFirst()
//                .ifPresent(notification -> notification.read("Y"));
//        redisService.saveList(key, alarms);
//        return "알림 읽음 처리 되었습니다.";
//    }
}
