package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListGroupResponse {
    private Long id;
    private Member member;
    private String title;
    private int runningTime;
    private LocalDateTime voteDeadline;
    private LocalDateTime confirmedDate;
    private int participants;
    private String isConfirmed;
    private String isDeleted;

    public static ListGroupResponse from(Group group) {
        return ListGroupResponse.builder()
                .id(group.getId())
                .member(group.getMember())
                .title(group.getTitle())
                .runningTime(group.getRunningTime())
                .voteDeadline(group.getVoteDeadline())
                .confirmedDate(group.getConfirmedDate())
                .participants(group.getParticipants())
                .isConfirmed(group.getIsConfirmed())
                .isDeleted(group.getIsDeleted())
                .build();
    }
}
