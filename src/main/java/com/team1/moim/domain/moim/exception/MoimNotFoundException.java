package com.team1.moim.domain.moim.exception;

import com.team1.moim.global.error.ErrorCode;
import jakarta.persistence.EntityNotFoundException;

public class MoimNotFoundException extends EntityNotFoundException {
    public MoimNotFoundException() {
        super(ErrorCode.MOIM_NOT_FOUND.getMessage());
    }
}
