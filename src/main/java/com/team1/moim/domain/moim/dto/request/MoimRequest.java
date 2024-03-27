package com.team1.moim.domain.moim.dto.request;

import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class MoimRequest {

    @NotEmpty(message = "제목을 입력하세요")
    private String title;

//    private List<MoimInfo> moimInfos;

    private String place;

    @NotNull(message = "모임의 예상 활동 시간을 입력하세요")
    @Min(1)
    private int runningTime;

    @NotEmpty(message = "가능한 모임 날짜들 중 첫 시작일을 입력하세요")
    private String expectStartDate;

    @NotEmpty(message = "가능한 모임 날짜들 중 마지막 날짜를 입력하세요")
    private String expectEndDate;

    @NotEmpty(message = "가능한 모임 시간대의 시작 시간을 입력하세요")
    private String expectStartTime;

    @NotEmpty(message = "가능한 모임 시간대의 마지막 시간을 입력하세요")
    private String expectEndTime;

    @NotEmpty(message = "모임에 참여할 수 있는 마감 시간을 지정하세요")
    private String voteDeadline;

    private String contents;

    private String filePath;

    public static Moim toEntity(String title,
//                                List<MoimInfo> moimInfos,
                                String place,
                                int runningTime,
                                String expectStartDate,
                                String expectEndDate,
                                String expectStartTime,
                                String expectEndTime,
                                String voteDeadline,
                                String contents,
                                String filePath) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        LocalDate parsedStartDate = LocalDate.parse(expectStartDate, dateFormatter);
        LocalDate parsedEndDate = LocalDate.parse(expectEndDate, dateFormatter);

        LocalTime parsedStartTime = LocalTime.parse(expectStartTime, timeFormatter);
        LocalTime parsedEndTime = LocalTime.parse(expectEndTime, timeFormatter);

        LocalDateTime parsedVoteDeadline = LocalDateTime.parse(voteDeadline, dateTimeFormatter);

        return Moim.builder()
                .title(title)
//                .moimInfos(moimInfos)
                .place(place)
                .runningTime(runningTime)
                .expectStartDate(parsedStartDate)
                .expectEndDate(parsedEndDate)
                .expectStartTime(parsedStartTime)
                .expectEndTime(parsedEndTime)
                .voteDeadline(parsedVoteDeadline)
                .contents(contents)
                .filePath(filePath)
                .build();
    }
}
