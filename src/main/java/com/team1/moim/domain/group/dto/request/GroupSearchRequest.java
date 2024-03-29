package com.team1.moim.domain.group.dto.request;

import lombok.Data;

@Data
public class GroupSearchRequest {
    // 확정, 대기, 조율 조건 관련
    private boolean filterConfirmed;
    private boolean filterWaiting;
    private boolean filterAdjusting;
}
