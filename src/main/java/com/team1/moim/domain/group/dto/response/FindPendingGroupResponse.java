package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupInfo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindPendingGroupResponse {

    private Long id;
    private String isConfirmed;
    private String title;
    private List<GroupInfo> groupInfos;
    private String place;
    private LocalDateTime voteDeadline;
    private String contents;
    private String filePath;

    public static FindPendingGroupResponse from(Group group) {
        return FindPendingGroupResponse.builder()
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
