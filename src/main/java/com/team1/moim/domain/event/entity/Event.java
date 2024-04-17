package com.team1.moim.domain.event.entity;

import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.global.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    제목
    @Column(nullable = false)
    private String title;

//    내용
    @Column(nullable = false)
    private String memo;

//    시작일자
    @Column(nullable = false)
    private LocalDateTime startDateTime;

//    종료일자
    @Column(nullable = false)
    private LocalDateTime endDateTime;

//    장소
    @Column
    private String place;

//    사분면
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Matrix matrix;

//    첨부파일
    @Column
    private String fileUrl;

//    삭제여부
    @Column(nullable = false)
    private String deleteYn = "N";

//    반복여부
    @Column
    private Long repeatParent;

//    알림여부
    @Column(nullable = false)
    private String alarmYn = "N";

    //    회원ID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ToDoList> toDoLists = new ArrayList<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Alarm> alarms = new ArrayList<>();

    @Builder
    public Event(String title, 
                 String memo, 
                 LocalDateTime startDateTime,
                 LocalDateTime endDateTime,
                 String place, 
                 Matrix matrix, 
                 String fileUrl, 
                 Long repeatParent, 
                 Member member, 
                 String alarmYn) {
        this.title = title;
        this.memo = memo;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.place = place;
        this.matrix = matrix;
        this.fileUrl = fileUrl;
        this.member = member;
        this.alarmYn = alarmYn;
        this.repeatParent = repeatParent;
    }

    // 일정 수정
    public void update(String title, String memo, String startDate, String endDate, String place, Matrix matrix, String fileUrl) {
        
        LocalDateTime LocalStart = LocalDateTime.parse(startDate);
        LocalDateTime LocalEnd = LocalDateTime.parse(endDate);

        this.title = title;
        this.memo = memo;
        this.startDateTime = LocalStart;
        this.endDateTime = LocalEnd;
        this.place = place;
        this.matrix = matrix;
        this.fileUrl = fileUrl;
    }

    public void attachMember(Member member) {
        this.member = member;
        member.getEvents().add(this);
    }

    public void changeData(LocalDateTime newStartDateTime,
                           LocalDateTime newEndDateTime,
                           Long repeatParent) {
        this.startDateTime = newStartDateTime;
        this.endDateTime = newEndDateTime;
        this.repeatParent = repeatParent;
    }

    // 일정 삭제
    public void delete() {
        this.deleteYn = "Y";
    }

}
