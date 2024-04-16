package com.team1.moim.domain.event.dto.request;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.ToDoList;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ToDoListRequest {

    @NotEmpty(message = "내용이 비어있으면 안됩니다.")
    private String contents;

    private String isChecked;

    public ToDoList toEntity(Event event){
        return ToDoList.builder()
                .contents(contents)
                .isChecked(isChecked)
                .event(event)
                .build();
    }
}
