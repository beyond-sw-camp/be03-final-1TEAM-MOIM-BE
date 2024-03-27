package com.team1.moim.domain.group.service;

import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.group.exception.GroupNotFoundException;
import com.team1.moim.domain.group.repository.GroupInfoRepository;
import com.team1.moim.domain.group.repository.GroupRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final MemberRepository memberRepository;

    // 모임 생성하기
    @Transactional
    public GroupDetailResponse create(
            GroupRequest groupRequest,
            List<GroupInfoRequest> groupInfoRequests) {

        Group group = GroupRequest.toEntity(
                groupRequest.getTitle(),
                groupRequest.getPlace(),
                groupRequest.getRunningTime(),
                groupRequest.getExpectStartDate(),
                groupRequest.getExpectEndDate(),
                groupRequest.getExpectStartTime(),
                groupRequest.getExpectEndTime(),
                groupRequest.getVoteDeadline(),
                groupRequest.getContents(),
                groupRequest.getFilePath()
        );

        groupRepository.save(group);

        if (groupInfoRequests != null) {
            for (GroupInfoRequest groupInfoRequest : groupInfoRequests) {
                Member member = memberRepository.findByEmail(groupInfoRequest.getMemberEmail())
                        .orElseThrow(MemberNotFoundException::new);
                GroupInfo groupInfo = GroupInfoRequest.toEntity(group, member);
                groupInfoRepository.save(groupInfo);
            }
        }

        return GroupDetailResponse.from(group);
    }

    // 모임 삭제
    @Transactional
    public void delete(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(GroupNotFoundException::new);
        group.delete();
    }

    // 모임 조회(일정 확정 전)
    @Transactional
    public FindPendingGroupResponse findPendingGroup(Long id) {
        Group pendingGroup = groupRepository.findByIsConfirmedAndIsDeletedAndId("N", "N", id);
        return FindPendingGroupResponse.from(pendingGroup);
    }

}
