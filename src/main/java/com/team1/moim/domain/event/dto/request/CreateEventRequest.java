package com.team1.moim.domain.event.dto.request;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Matrix;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Data
public class CreateEventRequest {

    @NotEmpty(message = "제목이 비어있으면 안됩니다.")
    private String title;

    @NotEmpty(message = "내용이 비어있으면 안됩니다.")
    private String memo;

    @NotEmpty(message = "시작일자가 비어있으면 안됩니다.")
    private LocalDateTime startDate;

    @NotEmpty(message = "종료일자가 비어있으면 안됩니다.")
    private LocalDateTime endDate;

    private String place;

    @NotEmpty(message = "매트릭스가 비어있으면 안됩니다.")
    private Matrix matrix;

    private MultipartFile file;

    private String repeatYn;

    private String alarmYn;

    public static Event toEntity(String title, String memo, LocalDateTime startDate, LocalDateTime endDate, String place, Matrix matrix, Path path){
        String fileUrl = path != null ? path.toString() : null;
        return Event.builder()
                .title(title)
                .memo(memo)
                .startDate(startDate)
                .endDate(endDate)
                .place(place)
                .matrix(matrix)
                .fileUrl(fileUrl)
                .build();
    }
}
