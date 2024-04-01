package com.team1.moim.global.config.sse.dto.response;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationResponse {
    private String nickname;
    private String message;
    private LocalDateTime sendTime;

    public static NotificationResponse from(Alarm alarm, Member member, LocalDateTime sendTime){
        String message = alarm.getSetTime() + alarm.getAlarmtype().toString()+"전 알람입니다.";
        return NotificationResponse.builder()
                .nickname(member.getNickname())
                .message(message)
                .sendTime(sendTime)
                .build();
    }
}
