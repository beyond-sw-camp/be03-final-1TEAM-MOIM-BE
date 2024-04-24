package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ListGroupResponse {
    private Long id;
    private String title;
    private int runningTime;
    private LocalDateTime voteDeadline;
    private LocalDateTime confirmedDate;
    private int participants;
    private String isConfirmed;
    private String isDeleted;
    private String hostEmail;
    private String hostNickname;
    private List<String[]> guestEmailNicknameIsAgreed;


    public static ListGroupResponse from(Group group, List<String[]> guestEmailNicknameIsAgreed) {
        return ListGroupResponse.builder()
                .id(group.getId())
                .title(group.getTitle())
                .runningTime(group.getRunningTime())
                .voteDeadline(group.getVoteDeadline())
                .confirmedDate(group.getConfirmedDateTime())
                .participants(group.getParticipants())
                .isConfirmed(group.getIsConfirmed())
                .isDeleted(group.getIsDeleted())
                .hostEmail(group.getMember().getEmail())
                .hostNickname(group.getMember().getNickname())
                .guestEmailNicknameIsAgreed(guestEmailNicknameIsAgreed)
                .build();
    }
}
