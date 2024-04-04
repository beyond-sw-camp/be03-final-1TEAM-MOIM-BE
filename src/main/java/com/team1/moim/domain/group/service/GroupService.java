package com.team1.moim.domain.group.service;

import com.team1.moim.domain.group.dto.request.GroupCreateAlarmRequest;
import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.request.GroupSearchRequest;
import com.team1.moim.domain.group.dto.response.FindConfirmedGroupResponse;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.dto.response.ListGroupResponse;
import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupAlarm;
import com.team1.moim.domain.group.entity.GroupAlarmTimeType;
import com.team1.moim.domain.group.entity.GroupAlarmType;
import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.group.exception.GroupInfoNotFoundException;
import com.team1.moim.domain.group.exception.GroupNotFoundException;
import com.team1.moim.domain.group.exception.ParticipantRequiredException;
import com.team1.moim.domain.group.repository.GroupAlarmRepository;
import com.team1.moim.domain.group.repository.GroupInfoRepository;
import com.team1.moim.domain.group.repository.GroupRepository;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.s3.S3Service;
import com.team1.moim.global.config.sse.dto.GroupNotificationResponse;
import com.team1.moim.global.config.sse.service.SseService;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupAlarmRepository groupAlarmRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final MemberRepository memberRepository;
    private final S3Service s3Service;
    private final SseService sseService;

    // 모임 생성하기
    @Transactional
    public GroupDetailResponse create(
            GroupRequest groupRequest,
            List<GroupInfoRequest> groupInfoRequests,
            List<GroupCreateAlarmRequest> groupCreateAlarmRequests) {

        Member host = getHostByEmail();

        // 참여자 정보
        if (groupInfoRequests == null || groupInfoRequests.isEmpty()) {
            throw new ParticipantRequiredException();
        }
        Group newGroup = groupRequest.toEntity(host, groupInfoRequests);
        for (GroupInfoRequest groupInfoRequest : groupInfoRequests) {
            log.info("참여자 존재여부 확인");
            Member participant = memberRepository.findByEmail(groupInfoRequest.getMemberEmail())
                    .orElseThrow(MemberNotFoundException::new);
            log.info("참여자 이메일: {}", participant.getEmail());
            GroupInfo groupInfo = groupInfoRequest.toEntity(participant);
            groupInfo.attachGroup(newGroup);
        }

        // 첨부파일
        String filePath = null;
        if (groupRequest.getFilePath() != null) {
            log.info("S3에 이미지 업로드: {}", filePath);
            filePath = s3Service.uploadFile("groups", groupRequest.getFilePath());
        }
        newGroup.setFilePath(filePath);
        groupRepository.save(newGroup);

        // 모임 생성 완료와 동시에 참여자들에게 알림 전송
        String hostname = host.getNickname();
        String groupTitle = newGroup.getTitle();
        String message = String.format("%s님이 \"%s\" 모임에 초대했습니다. 참여하시겠습니까?", hostname, groupTitle);
        log.info("메시지 내용 확인: " + message);
        for (GroupInfoRequest groupInfoRequest : groupInfoRequests) {
            String participantEmail = groupInfoRequest.getMemberEmail();
            log.info("참여자 이메일 주소: " + participantEmail);
            sseService.sendInstantAlarm(participantEmail, message);
        }

        // Deadline 임박에 대한 알림 추가(여러 개의 알림 등록 가능)
        if (groupCreateAlarmRequests != null) {
            for (GroupCreateAlarmRequest groupCreateAlarmRequest : groupCreateAlarmRequests) {
                GroupAlarmTimeType groupAlarmTimeType;
                if (groupCreateAlarmRequest.getAlarmTimeType().equals("MIN")) {
                    groupAlarmTimeType = GroupAlarmTimeType.MIN;
                } else if (groupCreateAlarmRequest.getAlarmTimeType().equals("HOUR")) {
                    groupAlarmTimeType = GroupAlarmTimeType.HOUR;
                } else {
                    groupAlarmTimeType = GroupAlarmTimeType.DAY;
                }

                GroupAlarm groupAlarm = GroupCreateAlarmRequest.toEntity(
                        newGroup,
                        groupCreateAlarmRequest.getDeadlineAlarm(),
                        groupAlarmTimeType,
                        GroupAlarmType.MOIM_DEADLINE);
                groupAlarmRepository.save(groupAlarm);
            }
        }

        return GroupDetailResponse.from(newGroup);
    }

    // 호스트 이메일
    private Member getHostByEmail() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
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

    // 그룹 알림 전송 스케줄러
    // 모임 참여 결정에 대한 마감 시간 알림을 제공한다.
    @Transactional
    @Scheduled(cron = "0 0/1 * * * *")
    public void scheduleGroupAlarm() {
        // 삭제되지 않은 그룹을 검색
        List<Group> groups = groupRepository.findByIsDeleted("N");
        for (Group group : groups) {
            // 모임 알림 중에서 스케줄러가 필요한 알림은 데드라인 마감 알림 밖에 없다.
            // 그 중에서 아직 알림을 보내지 않은 것을 선택한다.
            List<GroupAlarm> groupAlarms = groupAlarmRepository
                    .findByGroupAlarmTypeAndSendYn(GroupAlarmType.MOIM_DEADLINE, "N");

            for (GroupAlarm groupAlarm : groupAlarms) {

                // 알림을 Day 타입으로 한 경우
                if (groupAlarm.getGroupAlarmTimeType() == GroupAlarmTimeType.DAY
                        && groupAlarm.getGroup().getVoteDeadline().minusDays(groupAlarm.getDeadlineAlarm()).isBefore(LocalDateTime.now())) {
                    sendAlarmForParticipants(groupAlarm);
                }

                // 알림을 시간(H) 타입으로 한 경우
                else if (groupAlarm.getGroupAlarmTimeType() == GroupAlarmTimeType.HOUR
                        && group.getVoteDeadline().minusHours(groupAlarm.getDeadlineAlarm()).isBefore(LocalDateTime.now())) {
                    sendAlarmForParticipants(groupAlarm);
                }

                // 알림을 분(M) 타입으로 한 경우
                else if (groupAlarm.getGroupAlarmTimeType() == GroupAlarmTimeType.MIN
                        && group.getVoteDeadline().minusMinutes(groupAlarm.getDeadlineAlarm()).isBefore(LocalDateTime.now())) {
                    sendAlarmForParticipants(groupAlarm);
                }
            }
        }

    }

    private void sendAlarmForParticipants(GroupAlarm groupAlarm) {
        // 알림은 아직 참여 또는 거절을 선택하지 않은 상태이고, 존재하는 유저한테만 보내야 한다.
        List<GroupInfo> participants = groupInfoRepository.findByGroupAndIsAgreedAndIsDeleted(
                groupAlarm.getGroup(), "P", "N");
        for (GroupInfo participant : participants) {
            sseService.sendGroupAlarm(participant.getMember().getEmail(), GroupNotificationResponse.from(groupAlarm));
        }
        groupAlarm.sendCheck("Y");
    }
}
