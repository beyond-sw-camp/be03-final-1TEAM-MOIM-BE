package com.team1.moim.domain.group.dto.request;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.util.DateTimeFormatterUtil;
import com.team1.moim.domain.member.entity.Member;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@NoArgsConstructor
public class GroupRequest {

    @NotEmpty(message = "제목을 입력하세요")
    private String title;

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

    private MultipartFile filePath;

    public Group toEntity(Member member, List<GroupInfoRequest> requests) {

        return Group.builder()
                .member(member)
                .title(title)
                .place(place)
                .runningTime(runningTime)
                .expectStartDate(DateTimeFormatterUtil.parseDate(expectStartDate))
                .expectEndDate(DateTimeFormatterUtil.parseDate(expectEndDate))
                .expectStartTime(DateTimeFormatterUtil.parseTime(expectStartTime))
                .expectEndTime(DateTimeFormatterUtil.parseTime(expectEndTime))
                .voteDeadline(DateTimeFormatterUtil.parseDateTime(voteDeadline))
                .contents(contents)
                .participants(requests.size())
                .build();
    }
}
