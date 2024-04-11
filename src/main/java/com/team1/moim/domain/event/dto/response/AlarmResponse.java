package com.team1.moim.domain.event.dto.response;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.event.entity.AlarmType;
import com.team1.moim.domain.event.entity.Event;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AlarmResponse {
    private Long id;
    private int setTime;
    private AlarmType alarmType;

    public static AlarmResponse from(Alarm alarm){
        return AlarmResponse.builder()
                .id(alarm.getId())
                .setTime(alarm.getSetTime())
                .alarmType(alarm.getAlarmtype())
                .build();
    }
}
