package com.team1.moim.domain.group.dto.response;

import com.team1.moim.domain.group.entity.Group;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindPendingGroupResponse {

    private Long id;
    private String isConfirmed;
    private String title;
    private List<GroupInfoResponse> groupInfos;
    private String place;
    private LocalDateTime voteDeadline;
    private String contents;
    private String filePath;

    public static FindPendingGroupResponse from(Group group) {
        // 1. group.getGroupInfos()는 Group 객체에서 GroupInfo 객체의 리스트를 가져옴
        // 2. stream()은 리스트를 스트림으로 변환.
        // 3. map(GroupInfoResponse::from)은 각 GroupInfo 객체를 받아 GroupInfoResponse 객체를 생성
        // 4. collect(Collectors.toList())는 변환된 스트림의 모든 요소를 다시 리스트로 모은다.
        List<GroupInfoResponse> groupInfos = group.getGroupInfos().stream()
                .map(GroupInfoResponse::from)
                .collect(Collectors.toList());

        return FindPendingGroupResponse.builder()
                .id(group.getId())
                .isConfirmed(group.getIsConfirmed())
                .title(group.getTitle())
                .groupInfos(groupInfos)
                .place(group.getPlace())
                .voteDeadline(group.getVoteDeadline())
                .contents(group.getContents())
                .filePath(group.getFilePath())
                .build();
    }
}
