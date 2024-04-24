package com.team1.moim.global.config.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.team1.moim.domain.event.dto.response.AvailableResponse;
import com.team1.moim.domain.notification.dto.GroupNotification;
import com.team1.moim.domain.notification.dto.EventNotification;
import com.team1.moim.domain.notification.dto.NotificationResponseNew;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RedisService {


    private final RedisTemplate<String, Object> redisTemplate;
    @Qualifier("1")
    private final RedisTemplate<String, Object> redisTemplate1;

    @Qualifier("2")
    private final RedisTemplate<String, String> redisTemplate2;

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate,  @Qualifier("1") RedisTemplate<String, Object> redisTemplate1, @Qualifier("2") RedisTemplate<String, String> redisTemplate2) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate1 = redisTemplate1;
        this.redisTemplate2 = redisTemplate2;
    }

    public void setValues(String key, String data) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.set(key, data);
    }

    public void setValues(String key, String data, Duration duration) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        log.info("이메일, 인증 코드 redis에 세팅 시작");
        values.set(key, data, duration);
        log.info("redis에 이메일 인증코드 관련 정보 저장");
    }

    public void setEventList(String key, EventNotification eventNotification) throws JsonProcessingException {
        ListOperations<String, Object> alarms = redisTemplate1.opsForList();
        log.info("일정 알림 저장");
        alarms.leftPush(key, eventNotification);
        log.info("일정 알림 저장 성공");
    }

    public void setGroupList(String key, GroupNotification groupNotification) throws JsonProcessingException {
        ListOperations<String, Object> alarms = redisTemplate1.opsForList();
        log.info("모임 알림 저장");
        alarms.leftPush(key, groupNotification);
        log.info("모임 알림 저장 성공");
    }

    public void setAvailableList(String key, LocalDateTime availableDay) throws JsonProcessingException {
        ListOperations<String, String> availableList = redisTemplate2.opsForList();
        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        String formattedDateTime = availableDay.format(formatter);
        log.info("추천 일정 저장");
        availableList.leftPush(key, formattedDateTime);
        log.info("추천 일정 저장 성공");
    }

    @Transactional(readOnly = true)
    public String getValues(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        if (values.get(key) == null) {
            return "false";
        }
        return (String) values.get(key);
    }

    public List<NotificationResponseNew> getList(String key){
        ListOperations<String, Object> listOperations = redisTemplate1.opsForList();
        List<Object> alarms = listOperations.range(key, 0, -1);
        List<NotificationResponseNew> notificationResponses = new ArrayList<>();
        for(Object alarm : alarms) {
            if(alarm instanceof EventNotification) {
                notificationResponses.add(NotificationResponseNew.fromEvent((EventNotification) alarm));
            }
            if(alarm instanceof GroupNotification) {
                notificationResponses.add(NotificationResponseNew.fromGroup((GroupNotification) alarm));
            }

        }
        return notificationResponses;
    }

    public List<AvailableResponse> getAvailableList(String key){
        ListOperations<String, String> listOperations = redisTemplate2.opsForList();
        List<String> availableList = listOperations.range(key, 0, -1);
        List<AvailableResponse> AvailableResponses = new ArrayList<>();
        for(String day : availableList) {
            AvailableResponses.add(AvailableResponse.from(key, day));
        }
        return AvailableResponses;
    }

    public void saveList(String key, List<EventNotification> notificationResponses) {
        ListOperations<String, Object> listOperations = redisTemplate1.opsForList();
        // 기존 리스트 삭제
        redisTemplate1.delete(key);
        // 변경된 리스트 추가
        for (EventNotification notificationResponse : notificationResponses) {
            listOperations.leftPush(key, notificationResponse);
        }
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    public void expireValues(String key, int timeout) {
        redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
    }

    public void setHashOps(String key, Map<String, String> data) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.putAll(key, data);
    }

    @Transactional(readOnly = true)
    public String getHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        return Boolean.TRUE.equals(values.hasKey(key, hashKey)) ? (String) redisTemplate.opsForHash().get(key, hashKey) : "";
    }

    public void deleteHashOps(String key, String hashKey) {
        HashOperations<String, Object, Object> values = redisTemplate.opsForHash();
        values.delete(key, hashKey);
    }

    public boolean checkExistsValue(String value) {
        return !value.equals("false");
    }

    public void increment(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        values.increment(key);
    }

    public Set<String> keys(String pattern) {
        return redisTemplate.keys(pattern);
    }
}
