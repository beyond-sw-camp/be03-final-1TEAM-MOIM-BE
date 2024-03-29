package com.team1.moim.domain.event.dto.request;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.event.entity.AlarmType;
import com.team1.moim.domain.event.entity.Event;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class AlarmRequest {
    @NotEmpty(message = "설정 시간이 비어있으면 안됩니다.")
    private int setTime;
    @NotEmpty(message = "알림 타입이 비어있으면 안됩니다.")
    private String alarmType;

    public static Alarm toEntity(AlarmType alarmtype, int setTime, Event event){
        return Alarm.builder()
                .event(event)
                .alarmtype(alarmtype)
                .setTime(setTime)
                .build();
    }
}

