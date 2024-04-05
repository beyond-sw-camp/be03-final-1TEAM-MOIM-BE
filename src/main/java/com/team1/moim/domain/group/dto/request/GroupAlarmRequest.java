package com.team1.moim.domain.group.dto.request;

import com.team1.moim.domain.group.entity.GroupAlarm;
import com.team1.moim.domain.group.entity.GroupAlarmTimeType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class GroupAlarmRequest {
    
    // e.g., 1일 전, 3시간 전, 30분 전
    @NotEmpty(message = "마감시간 알림은 비어 있을 수 없습니다.")
    private int deadlineAlarm;
    
    // 일, 시간, 분 타입
    @NotEmpty(message = "알림 시간 타입이 비어있으면 안됩니다.")
    private String alarmTimeType;

    public GroupAlarm toEntity(GroupAlarmTimeType groupAlarmTimeType) {
        return GroupAlarm.builder()
                .deadlineAlarm(deadlineAlarm)
                .groupAlarmTimeType(groupAlarmTimeType)
                .build();
    }
}
