package com.team1.moim.domain.event.entity;

import com.team1.moim.global.config.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ToDoList extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    내용
    @Column(nullable = false)
    private String contents;

    //    완료여부
    @Column(nullable = false)
    private String isChecked = "N";

    //    EventID
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @Builder
    public ToDoList(Event event, String contents, String isChecked) {
        this.event = event;
        this.contents = contents;
        this.isChecked = isChecked;
    }

    // Event Entity에 To-Do 리스트 Entity 매핑
    public void attachEvent(Event event) {
        this.event = event;
        event.getToDoLists().add(this);
    }

    // 투두리스트 수정
    public void update(String contents, String isChecked) {
        this.contents = contents;
        this.isChecked = isChecked;
    }

}
