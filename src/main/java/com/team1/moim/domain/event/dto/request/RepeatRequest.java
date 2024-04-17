package com.team1.moim.domain.event.dto.request;


import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Repeat;
import com.team1.moim.domain.event.entity.RepeatType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RepeatRequest {

    @NotEmpty(message = "반복타입이 비어있으면 안됩니다.")
    private String repeatType;

    @NotEmpty(message = "반복 종료일자가 비어있으면 안됩니다.")
    private String repeatEndDate;

    public Repeat toEntity(RepeatType repeatType, Event event){
        LocalDate LocalEndDate = LocalDate.parse(repeatEndDate);

        return Repeat.builder()
                .repeatType(repeatType)
                .repeatEndDate(LocalEndDate)
                .event(event)
                .build();
    }


}
