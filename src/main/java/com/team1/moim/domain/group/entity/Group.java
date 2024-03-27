package com.team1.moim.domain.group.entity;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.global.config.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "groups")
public class Group extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 호스트ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 제목
    @Column(nullable = false)
    private String title;

    // 내용
    private String contents;

    // 활동시간
    @Column(nullable = false)
    private int runningTime;

    // 희망 시작 날짜
    @Column(nullable = false)
    private LocalDate expectStartDate;

    // 희망 종료 날짜
    @Column(nullable = false)
    private LocalDate expectEndDate;

    // 희망 시작 시간
    @Column(nullable = false)
    private LocalTime expectStartTime;

    // 희망 종료 시간
    @Column(nullable = false)
    private LocalTime expectEndTime;

    // 장소
    private String place;

    // 첨부파일 경로
    private String filePath;

    // 투표 마감 시간
    @Column(nullable = false)
    private LocalDateTime voteDeadline;

    // 최종 확정 일정
    private LocalDateTime confirmedDate;

    // 확정 여부 (Y, N)
    @Column(nullable = false)
    @Builder.Default
    private String isConfirmed = "N";

    // 참여자수
    private int voters;

    // 삭제여부 (Y, N)
    @Column(nullable = false)
    @Builder.Default
    private String isDeleted = "N";

    public void delete() {
        this.isDeleted = "Y";
    }
}
