package com.team1.moim.domain.event.dto.request;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Matrix;
import com.team1.moim.domain.member.entity.Member;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

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

    private List<ToDoListRequest> toDoListRequests;
    private RepeatRequest repeatValue;
    private List<AlarmRequest> alarmRequests;
    private String alarmYn;

    public Event toEntity(Member member, Matrix matrix, String fileUrl){

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
                .member(member)
                .alarmYn(alarmYn)
                .build();
    }

    public EventRequest changeDateRequest(Long repeatParentId){
        // 새로운 EventRequest 객체 생성 (깊은 복사는 필요한 필드만 수동으로 진행)
        EventRequest newRequest = new EventRequest();

        // 기존 request로부터 필요한 모든 필드를 새 객체로 복사
        newRequest.setTitle(title);
        newRequest.setMemo(memo);
        // 새로운 startDate와 endDate로 설정
        newRequest.setStartDate(startDate);
        newRequest.setEndDate(endDate);
        newRequest.setPlace(place);
        newRequest.setMatrix(matrix);
        newRequest.setFile(file);
        newRequest.setAlarmYn(alarmYn);

        // 수정된 객체 반환
        return newRequest;
    }
}
