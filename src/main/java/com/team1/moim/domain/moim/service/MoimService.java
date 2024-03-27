package com.team1.moim.domain.moim.service;

import com.team1.moim.domain.member.dto.request.MemberRequest;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.domain.moim.dto.request.MoimRequest;
import com.team1.moim.domain.moim.dto.request.MoimInfoRequest;
import com.team1.moim.domain.moim.dto.response.FindPendingMoimResponse;
import com.team1.moim.domain.moim.dto.response.MoimDetailResponse;
import com.team1.moim.domain.moim.entity.Moim;
import com.team1.moim.domain.moim.entity.MoimInfo;
import com.team1.moim.domain.moim.exception.MoimNotFoundException;
import com.team1.moim.domain.moim.repository.MoimInfoRepository;
import com.team1.moim.domain.moim.repository.MoimRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class MoimService {

    private final MoimRepository moimRepository;
    private final MoimInfoRepository moimInfoRepository;
    private final MemberRepository memberRepository;

    // 모임 생성하기
    @Transactional
    public MoimDetailResponse create(
            MoimRequest moimRequest,
            List<MoimInfoRequest> moimInfoRequests) {

        Moim moim = MoimRequest.toEntity(
                moimRequest.getTitle(),
//                moimRequest.getMoimInfos(),
                moimRequest.getPlace(),
                moimRequest.getRunningTime(),
                moimRequest.getExpectStartDate(),
                moimRequest.getExpectEndDate(),
                moimRequest.getExpectStartTime(),
                moimRequest.getExpectEndTime(),
                moimRequest.getVoteDeadline(),
                moimRequest.getContents(),
                moimRequest.getFilePath()
        );

//        if (moimInfoRequests != null) {
//            for (MoimInfoRequest moimInfoRequest : moimInfoRequests) {
//                MoimInfo moimInfo = MoimInfoRequest.toEntity(
//                        moim, member, moimInfoRequest.getIsVoted(), moimInfoRequest.getIsAgreed());
//                moimInfoRepository.save(moimInfo);
//            }
//        }
        if (moimInfoRequests != null) {
            for (MoimInfoRequest moimInfoRequest : moimInfoRequests) {
                Member member = memberRepository.findByEmail(moimInfoRequest.getMemberEmail())
                        .orElseThrow(MemberNotFoundException::new);
                MoimInfo moimInfo = MoimInfoRequest.toEntity(moim, member);
//                moim.getMoimInfos().add(moimInfo);
                moimInfoRepository.save(moimInfo);
            }
        }

        return MoimDetailResponse.from(moimRepository.save(moim));
    }

    // 모임 삭제
    @Transactional
    public void delete(Long id) {
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
