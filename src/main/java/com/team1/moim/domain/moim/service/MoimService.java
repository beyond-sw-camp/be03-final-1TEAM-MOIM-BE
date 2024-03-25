package com.team1.moim.domain.moim.service;

import com.team1.moim.domain.moim.dto.request.MoimCreateRequest;
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
        Moim newMoim = moimCreateRequest.toEntity();
        return MoimDetailResponse.from(moimRepository.save(newMoim));
    }

    // 모임 삭제
    @Transactional
    public void deleteMoim(Long id) {
        Moim moim = moimRepository.findById(id).orElseThrow(MoimNotFoundException::new);
        moim.delete();
    }

}
