package com.team1.moim.domain.event.dto.response;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Matrix;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EventResponse {
    private Long id;
    private String title;
    private String memo;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String place;
    private Matrix matrix;
    private String fileUrl;
    private String deleteYn;
    private Long repeatParent;
    private String alarmYn;

    public static EventResponse from(Event event){
        return EventResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .memo(event.getMemo())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .place(event.getPlace())
                .matrix(event.getMatrix())
                .fileUrl(event.getFileUrl())
                .deleteYn(event.getDeleteYn())
                .repeatParent(event.getRepeatParent())
                .alarmYn(event.getAlarmYn())
                .build();
    }

}
