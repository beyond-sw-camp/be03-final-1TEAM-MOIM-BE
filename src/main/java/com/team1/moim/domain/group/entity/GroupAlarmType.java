package com.team1.moim.domain.group.entity;

public enum GroupAlarmType {
    MOIM_CREATED,           // 모임이 생성되었을 때
    MOIM_ALL_PARTICIPATED,  // 모임의 모든 참여자들이 참여 확정버튼을 눌렀을 때
    MOIM_TIME_CONFIRMED,    // 모임 시간이 확정되었을 때
    MOIM_DEADLINE,          // 모임 참여 여부 확정에 대한 마감시간이 임박했을 때
}
