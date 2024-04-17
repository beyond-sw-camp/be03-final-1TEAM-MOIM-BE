package com.team1.moim.domain.event.dto.request;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Matrix;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventRequest {

    @NotEmpty(message = "제목이 비어있으면 안됩니다.")
    private String title;

    @NotEmpty(message = "내용이 비어있으면 안됩니다.")
    private String memo;

    @NotEmpty(message = "시작일자가 비어있으면 안됩니다.")
    private String startDate;

    @NotEmpty(message = "종료일자가 비어있으면 안됩니다.")
    private String endDate;

    private String place;

    @NotEmpty(message = "매트릭스가 비어있으면 안됩니다.")
    private String matrix;
    private String alarmYn;

    public Event toEntity(Matrix matrix, String fileUrl){

        LocalDateTime localStart = LocalDateTime.parse(startDate);
        LocalDateTime localEnd = LocalDateTime.parse(endDate);

        return Event.builder()
                .title(title)
                .memo(memo)
                .startDateTime(localStart)
                .endDateTime(localEnd)
                .place(place)
                .matrix(matrix)
                .fileUrl(fileUrl)
                .alarmYn(alarmYn)
                .build();
    }
}
