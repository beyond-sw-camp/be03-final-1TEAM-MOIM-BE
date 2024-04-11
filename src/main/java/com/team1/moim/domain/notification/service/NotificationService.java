package com.team1.moim.domain.notification.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team1.moim.domain.event.dto.request.AlarmRequest;
import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.RepeatRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.AlarmResponse;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.*;
import com.team1.moim.domain.event.exception.EventNotFoundException;
import com.team1.moim.domain.event.repository.AlarmRepository;
import com.team1.moim.domain.event.repository.EventRepository;
import com.team1.moim.domain.event.repository.RepeatRepository;
import com.team1.moim.domain.event.repository.ToDoListRepository;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.exception.MemberNotMatchException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.domain.notification.exception.NotificationNotFoundException;
import com.team1.moim.global.config.redis.RedisService;
import com.team1.moim.global.config.s3.S3Service;
import com.team1.moim.global.config.sse.dto.NotificationResponse;
import com.team1.moim.global.config.sse.service.SseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableAsync // 일정이 먼저 만들어지고 반복일정 메소드는 따로 만들어지도록 추가함
public class NotificationService {

    private static final String FILE_TYPE = "events";

    private final MemberRepository memberRepository;
    private final RedisService redisService;

    public List<NotificationResponse> getAlarms(Long memberId) {
        Member findMember = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        String key = findMember.getEmail();
        String loginEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!key.equals(loginEmail)) throw new MemberNotMatchException();
        List<NotificationResponse> alarms= redisService.getList(key);
        if(alarms.isEmpty()) throw new NotificationNotFoundException();
        log.info(alarms.size() + "개");
        return alarms;
    }

    public String readAlarm(Long memberId, Long alarmId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        log.info(member.getNickname() + "회원");
        String key = member.getEmail();
        String loginEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if(!key.equals(loginEmail)) throw new MemberNotMatchException();
        List<NotificationResponse> alarms= redisService.getList(key);
        alarms.stream()
                .filter(notification -> Objects.equals(notification.getAlarmId(), alarmId))
                .findFirst()
                .ifPresent(notification -> notification.read("Y"));
        redisService.saveList(key, alarms);
        return "알림 읽음 처리 되었습니다.";
    }
}
