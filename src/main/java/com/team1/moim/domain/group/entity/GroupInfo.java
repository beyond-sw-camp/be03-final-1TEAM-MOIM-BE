package com.team1.moim.domain.group.entity;

import com.team1.moim.domain.member.entity.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class GroupInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 모임 식별 번호
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "groups_id")
    private Group group;

    // 회원 ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 투표 현황
    @Builder.Default
    @Column(nullable = false)
    private String isVoted = "N";

    // 동의 여부
    @Builder.Default
    @Column(nullable = false)
    private String isAgreed = "N";

    // 삭제여부 (Y, N)
    @Column(nullable = false)
    @Builder.Default
    private String isDeleted = "N";

    public void delete() {
        this.isDeleted = "Y";
    }
}
