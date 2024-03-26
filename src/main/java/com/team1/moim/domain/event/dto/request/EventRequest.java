package com.team1.moim.domain.event.dto.request;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Matrix;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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

    private MultipartFile file;

    private String repeatYn;

    private String alarmYn;

    public static Event toEntity(String title, String memo, String startDate, String endDate, String place, Matrix matrix, String fileUrl){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime LocalStart = LocalDateTime.parse(startDate, formatter);
        LocalDateTime LocalEnd = LocalDateTime.parse(endDate, formatter);

        return Event.builder()
                .title(title)
                .memo(memo)
                .startDate(LocalStart)
                .endDate(LocalEnd)
                .place(place)
                .matrix(matrix)
                .fileUrl(fileUrl)
                .build();
    }
}
