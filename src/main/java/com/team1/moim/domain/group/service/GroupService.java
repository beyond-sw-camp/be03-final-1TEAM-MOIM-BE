package com.team1.moim.domain.group.service;

import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.request.GroupSearchRequest;
import com.team1.moim.domain.group.dto.response.FindConfirmedGroupResponse;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.dto.response.ListGroupResponse;
import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.group.exception.GroupInfoNotFoundException;
import com.team1.moim.domain.group.exception.GroupNotFoundException;
import com.team1.moim.domain.group.repository.GroupInfoRepository;
import com.team1.moim.domain.group.repository.GroupRepository;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
            List<GroupInfoRequest> groupInfoRequests,
            String loginEmail) {

        // 로그인한 사용자의 이메일로 Member를 조회
        Member loginedMember = memberRepository.findByEmail(loginEmail)
                .orElseThrow(MemberNotFoundException::new);

        groupRequest.setMember(loginedMember);

        Group group = GroupRequest.toEntity(
                groupRequest.getMember(),
                groupRequest.getTitle(),
                groupRequest.getPlace(),
                groupRequest.getRunningTime(),
                groupRequest.getExpectStartDate(),
                groupRequest.getExpectEndDate(),
                groupRequest.getExpectStartTime(),
                groupRequest.getExpectEndTime(),
                groupRequest.getVoteDeadline(),
                groupRequest.getContents(),
                groupRequest.getFilePath(),
                groupInfoRequests
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
        for (GroupInfo groupInfo : groupInfoRepository.findByGroup(group)
                .orElseThrow(GroupInfoNotFoundException::new)) {
            groupInfo.delete();
        }
    }

    // 모임 조회(일정 확정 전)
    @Transactional
    public FindPendingGroupResponse findPendingGroup(Long id) {
        Group pendingGroup = groupRepository.findByIsConfirmedAndIsDeletedAndId("N", "N", id)
                .orElseThrow(GroupNotFoundException::new);
        return FindPendingGroupResponse.from(pendingGroup);
    }

    // 모임 조회(일정 확정 후)
    @Transactional
    public FindConfirmedGroupResponse findConfirmedGroup(Long id) {
        Group confirmedGroup = groupRepository.findByIsConfirmedAndIsDeletedAndId("Y", "N", id)
                .orElseThrow(GroupNotFoundException::new);
        return FindConfirmedGroupResponse.from(confirmedGroup);
    }

    // 전체 모임 조회하기
    @Transactional
    public List<ListGroupResponse> findGroups(
            GroupSearchRequest groupSearchRequest, Pageable pageable, String loginEmail) {

        Specification<Group> spec = new Specification<>() {
            @Override
            public Predicate toPredicate(Root<Group> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
                List<Predicate> predicates = new ArrayList<>();

                // Group 테이블과 GroupInfo 테이블을 조인하고, 그 결과에서 특정 멤버의 이메일이 loginEmail과 같은 그룹만 선택
                // SELECT * FROM Group g
                // JOIN GroupInfo gi ON g.id = gi.group_id
                // WHERE gi.member.email = :loginEmail
                Join<Group, GroupInfo> groupJoin = root.join("groupInfos");
                predicates.add(criteriaBuilder.equal(groupJoin.get("member").get("email"), loginEmail));

                // 삭제되지 않은 그룹
                predicates.add(criteriaBuilder.equal(root.get("isDeleted"), "N"));

                // filterConfirmed가 true일 경우, 명시적으로 "확정"(isConfirmed가 "Y")된 그룹을 검색
                // filterWaiting가 true일 경우, "대기" 상태(isConfirmed가 "N"이고 isAgreed가 "N")인 그룹을 검색
                // filterAdjusting가 true일 경우, "조율" 상태(isConfirmed가 "N"이고 isAgreed가 "Y")인 그룹을 검색
                // 사용자가 특정 상태의 그룹만 보고 싶다면, 해당 상태에 대한 필터를 true로 설정.
                // 모든 그룹을 확인하고 싶다면, 모든 필터를 false로 두거나 설정하지 않으면 됨.

                if (groupSearchRequest.isFilterConfirmed()) {
                    predicates.add(criteriaBuilder.equal(groupJoin.get("isConfirmed"), "Y"));
                } else if (groupSearchRequest.isFilterWaiting()) {
                    predicates.add(criteriaBuilder.equal(groupJoin.get("isConfirmed"), "N"));
                    predicates.add(criteriaBuilder.equal(groupJoin.get("isAgreed"), "N"));
                } else if (groupSearchRequest.isFilterAdjusting()) {
                    predicates.add(criteriaBuilder.equal(groupJoin.get("isConfirmed"), "N"));
                    predicates.add(criteriaBuilder.equal(groupJoin.get("isAgreed"), "Y"));
                }

                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            }
        };

        List<ListGroupResponse> groups = groupRepository.findAll(spec, pageable).stream()
                .map(ListGroupResponse::from)
                .collect(Collectors.toList());
        return groups;
    }
}
