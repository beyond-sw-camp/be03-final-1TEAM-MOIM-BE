package com.team1.moim.domain.moim.dto.response;

import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MoimDetailResponse {
    //투표현황, 제목, 참여자, 장소, 투표 마감일, 내용, 첨부파일
    private Long id;
    private String isConfirmed;
    private String title;
    private List<MoimInfo> moimInfos;
    private String place;
    private LocalDateTime voteDeadline;
    private String contents;
    private String filePath;

    public static MoimDetailResponse from(Moim moim) {
        return MoimDetailResponse.builder()
                .id(moim.getId())
                .isConfirmed(moim.getIsConfirmed())
                .title(moim.getTitle())
                .moimInfos(moim.getMoimInfos())
                .place(moim.getPlace())
                .voteDeadline(moim.getVoteDeadline())
                .contents(moim.getContents())
                .filePath(moim.getFilePath())
                .build();
    }
}
