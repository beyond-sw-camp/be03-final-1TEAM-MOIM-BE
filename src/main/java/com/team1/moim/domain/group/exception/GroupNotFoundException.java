package com.team1.moim.domain.group.exception;

import com.team1.moim.global.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.Getter;

@Getter
public class GroupNotFoundException extends EntityNotFoundException {

    private static final ErrorCode errorcode = ErrorCode.GROUP_NOT_FOUND;
}
