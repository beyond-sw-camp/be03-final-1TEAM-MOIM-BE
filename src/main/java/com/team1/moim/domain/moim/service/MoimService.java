package com.team1.moim.domain.moim.service;

import com.team1.moim.domain.moim.dto.request.MoimCreateRequest;
import com.team1.moim.domain.moim.dto.response.FindPendingMoimResponse;
import com.team1.moim.domain.moim.dto.response.MoimDetailResponse;
import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.exception.MoimNotFoundException;
import com.team1.moim.domain.moim.repository.MoimRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoimService {

    private final MoimRepository moimRepository;

    // 모임 생성하기
    @Transactional
    public MoimDetailResponse createMoim(MoimCreateRequest moimCreateRequest) {
        Moim newMoim = moimCreateRequest.toEntity(
                moimCreateRequest.getTitle(),
//                moimCreateRequest.getMoimInfos(),
                moimCreateRequest.getPlace(),
                moimCreateRequest.getRunningTime(),
                moimCreateRequest.getExpectStartDate(),
                moimCreateRequest.getExpectEndDate(),
                moimCreateRequest.getExpectStartTime(),
                moimCreateRequest.getExpectEndTime(),
                moimCreateRequest.getVoteDeadline(),
                moimCreateRequest.getContents(),
                moimCreateRequest.getFilePath()
        );
        return MoimDetailResponse.from(moimRepository.save(newMoim));
    }

    // 모임 삭제
    @Transactional
    public void deleteMoim(Long id) {
        Moim moim = moimRepository.findById(id).orElseThrow(MoimNotFoundException::new);
        moim.delete();
    }

    // 모임 조회(일정 확정 전)
    @Transactional
    public FindPendingMoimResponse findPendingMoim(Long id) {
        Moim pendingMoim = moimRepository.findByIsConfirmedAndIsDeletedAndId("N", "N", id);
        return FindPendingMoimResponse.from(pendingMoim);
    }



}
