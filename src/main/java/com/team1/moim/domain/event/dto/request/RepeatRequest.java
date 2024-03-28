package com.team1.moim.domain.event.dto.request;


import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Repeat;
import com.team1.moim.domain.event.entity.Repeat_type;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class RepeatRequest {
    @NotEmpty(message = "반복타입이 비어있으면 안됩니다.")
    private String reapetType;

    @NotEmpty(message = "반복 종료일자가 비어있으면 안됩니다.")
    private String reapet_end_date;

    public static Repeat toEntity(Repeat_type reapetType, String reapet_end_date, Event event){
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        LocalDate LocalEndDate = LocalDate.parse(reapet_end_date);


        return Repeat.builder()
                .repeatType(reapetType)
                .reapet_end_date(LocalEndDate)
                .event(event)
                .build();
    }


}
