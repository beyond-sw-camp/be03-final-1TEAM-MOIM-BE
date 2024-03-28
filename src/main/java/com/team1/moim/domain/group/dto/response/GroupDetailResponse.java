package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.Group;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupDetailResponse {
    private Long id;
    private String isConfirmed;
    private String title;
    private String place;
    private LocalDateTime voteDeadline;
    private String contents;
    private String filePath;

    public static GroupDetailResponse from(Group group) {
        return GroupDetailResponse.builder()
                .id(group.getId())
                .isConfirmed(group.getIsConfirmed())
                .title(group.getTitle())
                .place(group.getPlace())
                .voteDeadline(group.getVoteDeadline())
                .contents(group.getContents())
                .filePath(group.getFilePath())
                .build();
    }
}
