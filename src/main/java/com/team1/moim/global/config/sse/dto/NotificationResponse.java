package com.team1.moim.global.config.sse.dto;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;

import com.team1.moim.domain.member.entity.Role;
import com.team1.moim.domain.member.entity.SocialType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

@NoArgsConstructor
@Getter
public class NotificationResponse {
    @Id
    private Long alarmId;
    private String nickname;
    private String message;
    private String sendTime;
    private String readYn = "N";

    @Builder
    public NotificationResponse(Long alarmId, String nickname, String message, String sendTime) {
        this.alarmId = alarmId;
        this.nickname = nickname;
        this.message = message;
        this.sendTime = sendTime;
    }


    public static NotificationResponse from(Alarm alarm, Member member, LocalDateTime sendTime){
        String message = alarm.getSetTime() + alarm.getAlarmtype().toString()+"전 알람입니다.";
        return NotificationResponse.builder()
                .alarmId(alarm.getId())
                .nickname(member.getNickname())
                .message(message)
                .sendTime(sendTime.toString())
                .build();
    }
}
