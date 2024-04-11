package com.team1.moim.domain.notification.service;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.redis.RedisService;
import com.team1.moim.global.config.sse.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final MemberRepository memberRepository;
    private final RedisService redisService;

    public List<NotificationResponse> getAlarms(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        String key = member.getEmail();
        List<NotificationResponse> alarms= redisService.getList(key);
        return alarms;
    }

    public String readAlarm(Long memberId, Long alarmId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        log.info(member.getNickname() + "회원");
        String key = member.getEmail();
        List<NotificationResponse> alarms= redisService.getList(key);
        alarms.stream()
                .filter(notification -> Objects.equals(notification.getAlarmId(), alarmId))
                .findFirst()
                .ifPresent(notification -> notification.read("Y"));
        redisService.saveList(key, alarms);
        return alarms.toString();
    }
}
