package com.team1.moim.domain.moim.dto.request;

import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class MoimCreateRequest {

    @NotEmpty(message = "제목을 입력하세요")
    private String title;

    private List<MoimInfo> moimInfos;

    private String place;

    @NotEmpty(message = "모임의 예상 활동 시간을 입력하세요")
    private int runningTime;

    @NotEmpty(message = "가능한 모임 날짜들 중 첫 시작일을 입력하세요")
    private LocalDate expectStartDate;

    @NotEmpty(message = "가능한 모임 날짜들 중 마지막 날짜를 입력하세요")
    private LocalDate expectEndDate;

    @NotEmpty(message = "가능한 모임 시간대의 시작 시간을 입력하세요")
    private LocalTime expectStartTime;

    @NotEmpty(message = "가능한 모임 시간대의 마지막 시간을 입력하세요")
    private LocalTime expectEndTime;

    @NotEmpty(message = "모임에 참여할 수 있는 마감 시간을 지정하세요")
    private LocalDateTime voteDeadline;

    private String contents;

    private String filePath;

    public Moim toEntity() {
        return Moim.builder()
                .title(title)
                .moimInfos(moimInfos)
                .place(place)
                .runningTime(runningTime)
                .expectStartDate(expectStartDate)
                .expectEndDate(expectEndDate)
                .expectStartTime(expectStartTime)
                .expectEndTime(expectEndTime)
                .voteDeadline(voteDeadline)
                .contents(contents)
                .filePath(filePath)
                .build();
    }
}
