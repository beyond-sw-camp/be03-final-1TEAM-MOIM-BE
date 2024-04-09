package com.team1.moim.global.config.sse.dto;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.redis.core.RedisHash;

@Getter
@Builder
@RedisHash(timeToLive = 90L)
public class NotificationResponse {
    private Long id;
    private String nickname;
    private String message;
    private String sendTime;

    public static NotificationResponse from(Alarm alarm, Member member, LocalDateTime sendTime){
        String message = alarm.getSetTime() + alarm.getAlarmtype().toString()+"전 알람입니다.";
        return NotificationResponse.builder()
                .id(alarm.getId())
                .nickname(member.getNickname())
                .message(message)
                .sendTime(sendTime.toString())
                .build();
    }
}
