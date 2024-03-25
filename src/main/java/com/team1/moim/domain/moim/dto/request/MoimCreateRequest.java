package com.team1.moim.domain.moim.dto.request;

import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MoimCreateRequest {
    private String title;
    private List<MoimInfo> moimInfos; // 직접적인 엔티티 대신 moimInfo의 식별자 목록을 사용
    private String place;
    private int runningTime;
    private LocalDate expectStartDate;
    private LocalDate expectEndDate;
    private LocalTime expectStartTime;
    private LocalTime expectEndTime;
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
