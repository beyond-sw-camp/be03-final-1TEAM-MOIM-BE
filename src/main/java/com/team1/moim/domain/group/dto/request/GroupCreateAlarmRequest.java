package com.team1.moim.domain.group.dto.request;

import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupAlarm;
import com.team1.moim.domain.group.entity.GroupAlarmTimeType;
import com.team1.moim.domain.group.entity.GroupAlarmType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class GroupCreateAlarmRequest {
    
    // e.g., 1일 전, 3시간 전, 30분 전
    @NotEmpty(message = "마감시간 알림은 비어 있을 수 없습니다.")
    private int deadlineAlarm;
    
    // 일, 시간, 분 타입
    @NotEmpty(message = "알림 시간 타입이 비어있으면 안됩니다.")
    private String alarmTimeType;

//    // 모임 생성 시: MOIM_CREATED
//    // 모임 참여 결정에 대한 마감 시간 알림 설정 시: MOIM_DEADLINE
//    @NotEmpty(message = "알림 타입은 비어있을 수 없습니다.")
//    private String alarmType;

    public static GroupAlarm toEntity(
            Group group,
            int deadlineAlarm,
            GroupAlarmTimeType groupAlarmTimeType,
            GroupAlarmType groupAlarmType) {
        return GroupAlarm.builder()
                .group(group)
                .deadlineAlarm(deadlineAlarm)
                .groupAlarmTimeType(groupAlarmTimeType)
                .groupAlarmType(groupAlarmType)
                .build();
    }
}
