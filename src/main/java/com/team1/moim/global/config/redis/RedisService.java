package com.team1.moim.global.config.redis;

import com.team1.moim.global.config.sse.dto.NotificationResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
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

    @Autowired
    public RedisService(RedisTemplate<String, Object> redisTemplate,  @Qualifier("1") RedisTemplate<String, Object> redisTemplate1) {
        this.redisTemplate = redisTemplate;
        this.redisTemplate1 = redisTemplate1;
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

    public void setList(String key, String data) {
        ListOperations<String, Object> alarms = redisTemplate1.opsForList();
        log.info("List 알림 저장");
        alarms.leftPush(key, data);
        log.info("알림 저장 성공");
    }

    @Transactional(readOnly = true)
    public String getValues(String key) {
        ValueOperations<String, Object> values = redisTemplate.opsForValue();
        if (values.get(key) == null) {
            return "false";
        }
        return (String) values.get(key);
    }

    public List<Object> getList(String key){
        ListOperations<String, Object> listOperations = redisTemplate1.opsForList();
        List<Object> alarms = listOperations.range(key, 0, -1);
        for(Object alarm : alarms) {
            System.out.println(alarm.toString());
        }
        return alarms;
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
