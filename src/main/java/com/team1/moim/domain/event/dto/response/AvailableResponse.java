package com.team1.moim.domain.event.dto.response;

import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.event.entity.AlarmType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Builder
public class AvailableResponse {
    private Long groupId;
    private LocalDateTime availableDay;

    public static AvailableResponse from(String key, String availableDay){
        LocalDateTime convertAvailableDay = LocalDateTime.parse(availableDay, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return AvailableResponse.builder()
                .groupId(Long.parseLong(key))
                .availableDay(convertAvailableDay)
                .build();
    }
}
