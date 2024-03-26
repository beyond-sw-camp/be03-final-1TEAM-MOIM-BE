package com.team1.moim.domain.moim.dto.response;

import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FindPendingMoimResponse {

    private Long id;
    private String isConfirmed;
    private String title;
    private List<MoimInfo> moimInfos;
    private String place;
    private LocalDateTime voteDeadline;
    private String contents;
    private String filePath;

    public static FindPendingMoimResponse from(Moim moim) {
        return FindPendingMoimResponse.builder()
                .id(moim.getId())
                .isConfirmed(moim.getIsConfirmed())
                .title(moim.getTitle())
//                .moimInfos(moim.getMoimInfos())
                .place(moim.getPlace())
                .voteDeadline(moim.getVoteDeadline())
                .contents(moim.getContents())
                .filePath(moim.getFilePath())
                .build();
    }
}
