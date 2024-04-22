package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GroupDetailResponse {
    private Long id;
    private String hostNickname;
    private String isConfirmed;
    private String title;
    private String place;
    private LocalDateTime voteDeadline;
    private String contents;
    private String filePath;
    private int participants;

    public static GroupDetailResponse from(Group group) {
        return GroupDetailResponse.builder()
                .id(group.getId())
                .hostNickname(group.getMember().getNickname())
                .isConfirmed(group.getIsConfirmed())
                .title(group.getTitle())
                .place(group.getPlace())
                .voteDeadline(group.getVoteDeadline())
                .contents(group.getContents())
                .filePath(group.getFilePath())
                .participants(group.getParticipants())
                .build();
    }
}
