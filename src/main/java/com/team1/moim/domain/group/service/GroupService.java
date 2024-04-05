package com.team1.moim.domain.group.service;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.repository.EventRepository;
import com.team1.moim.domain.group.dto.request.GroupAlarmRequest;
import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.request.GroupSearchRequest;
import com.team1.moim.domain.group.dto.response.FindConfirmedGroupResponse;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.dto.response.ListGroupResponse;
import com.team1.moim.domain.group.entity.*;
import com.team1.moim.domain.group.exception.*;
import com.team1.moim.domain.group.repository.GroupAlarmRepository;
import com.team1.moim.domain.group.repository.GroupInfoRepository;
import com.team1.moim.domain.group.repository.GroupRepository;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.GroupInfoNotFoundException;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.domain.group.dto.response.VoteResponse;
import com.team1.moim.global.config.s3.S3Service;
import com.team1.moim.global.config.sse.dto.GroupNotification;
import com.team1.moim.global.config.sse.service.SseService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupAlarmRepository groupAlarmRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final S3Service s3Service;
    private final SseService sseService;

    // 모임 생성하기
    @Transactional
    public GroupDetailResponse create(
            GroupRequest groupRequest,
            List<GroupInfoRequest> groupInfoRequests,
            List<GroupAlarmRequest> groupAlarmRequests) {

        Member host = findMemberByEmail();

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

        // Deadline 임박에 대한 알림 추가(여러 개의 알림 등록 가능)
        if (groupAlarmRequests != null) {
            log.info("모임 알람 설정 진입");
            for (GroupAlarmRequest groupAlarmRequest : groupAlarmRequests) {
                GroupAlarmTimeType groupAlarmTimeType;
                if (groupAlarmRequest.getAlarmTimeType().equals("MIN")) {
                    log.info("모임 알람 분 설정");
                    groupAlarmTimeType = GroupAlarmTimeType.MIN;
                } else if (groupAlarmRequest.getAlarmTimeType().equals("HOUR")) {
                    groupAlarmTimeType = GroupAlarmTimeType.HOUR;
                } else {
                    groupAlarmTimeType = GroupAlarmTimeType.DAY;
                }
                GroupAlarm newGroupAlarm = groupAlarmRequest.toEntity(groupAlarmTimeType);
                newGroupAlarm.attachGroup(newGroup);
            }
        }

        groupRepository.save(newGroup);

        // 모임 생성 완료와 동시에 참여자들에게 알림 전송
        String hostname = host.getNickname();
        String groupTitle = newGroup.getTitle();
        String message = String.format("%s님이 \"%s\" 모임에 초대했습니다. 참여하시겠습니까?", hostname, groupTitle);
        log.info("메시지 내용 확인: " + message);

        List<GroupInfo> groupInfos = groupInfoRepository.findByGroup(newGroup);
        for (GroupInfo groupInfo : groupInfos) {
            String participantEmail = groupInfo.getMember().getEmail();
            log.info("참여자 이메일 주소: " + participantEmail);
            sseService.sendGroupNotification(participantEmail,
                    GroupNotification.from(newGroup, message));
        }

        return GroupDetailResponse.from(newGroup);
    }

    // 그룹 알림 전송 스케줄러
    // 모임 참여 결정에 대한 마감 시간 알림을 제공한다.
    @Transactional
    @Scheduled(cron = "0 0/1 * * * *")
    public void scheduleGroupAlarm() {
        // 삭제되지 않은 그룹 리스트를 검색
        List<Group> groups = groupRepository.findByIsDeleted("N");
        for (Group group : groups) {
            // 모임 알림 중에서 스케줄러가 필요한 알림은 데드라인 마감 알림 밖에 없다.
            // 그 중에서 아직 알림을 보내지 않은 것을 선택한다.
            List<GroupAlarm> groupAlarms = groupAlarmRepository
                    .findByGroupAndSendYn(group, "N");

            for (GroupAlarm groupAlarm : groupAlarms) {
                if (groupAlarm.getGroupAlarmTimeType() == GroupAlarmTimeType.DAY
                        && groupAlarm.getGroup().getVoteDeadline().minusDays(
                                groupAlarm.getDeadlineAlarm()).isBefore(LocalDateTime.now())){
                    sendAlarmForParticipants(groupAlarm);
                    return;
                }

                if (groupAlarm.getGroupAlarmTimeType() == GroupAlarmTimeType.HOUR
                        && groupAlarm.getGroup().getVoteDeadline().minusHours(
                                groupAlarm.getDeadlineAlarm()).isBefore(LocalDateTime.now())) {
                    sendAlarmForParticipants(groupAlarm);

                    return;
                }

                if (groupAlarm.getGroupAlarmTimeType() == GroupAlarmTimeType.MIN
                        && groupAlarm.getGroup().getVoteDeadline().minusMinutes(
                                groupAlarm.getDeadlineAlarm()).isBefore(LocalDateTime.now())) {
                    sendAlarmForParticipants(groupAlarm);

                    return;
                }
            }
        }
    }

    // 모임 삭제
    @Transactional
    public void delete(Long id) {
        Group group = groupRepository.findById(id).orElseThrow(GroupNotFoundException::new);
        group.delete();
        for (GroupInfo groupInfo : groupInfoRepository.findByGroup(group)) {
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

    @Transactional
    public VoteResponse vote(Long groupId, Long groupInfoId, String agreeYn){
        Member findParticipant = findMemberByEmail();
        Group findGroup = groupRepository.findById(groupId).orElseThrow(GroupNotFoundException::new);
        GroupInfo findGroupInfo =
                groupInfoRepository.findById(groupInfoId).orElseThrow(GroupInfoNotFoundException::new);

        // 로직 실행 전 이것 저것 검증(함수 내부에 검증 내용 주석으로 달아 놓음)
        validate(findParticipant, findGroup, findGroupInfo);

        // 수락 또는 거절 투표
        findGroupInfo.vote(agreeYn);

        // 변경 사항(동의 여부) 반영
        GroupInfo savedGroupInfo = groupInfoRepository.save(findGroupInfo);

        Group updatedGroup = savedGroupInfo.getGroup();

        if (checkVoteStatus(updatedGroup)){
            log.info("모임 참여자 전원 투표 완료!");

            // 모임 일정 자동 추천 로직 실행 후 추천 일정 리스트 가져오기
            List<LocalDateTime> recommendEvents = recommendGroupSchedule(savedGroupInfo.getGroup());
            log.info("추천 일정: " + recommendEvents);

            // 모임을 수락한 참여자 리스트 가져오기
            List<GroupInfo> agreedParticipants =
                    groupInfoRepository.findByGroupAndIsAgreed(savedGroupInfo.getGroup(), "Y");

            String groupTitle = savedGroupInfo.getGroup().getTitle();
            String message;
            // 모일 수 있는 시간이 없다면, 모두에게 모일 수 없다는 알림 발송
            if (recommendEvents.isEmpty()){
                message = groupTitle + " 모임이 취소되었습니다.";

                // Group 확정 및 삭제 처리
                updatedGroup.confirm();
                updatedGroup.delete();

                groupRepository.save(updatedGroup);

                // 호스트도 알림 발송
                sseService.sendGroupNotification(updatedGroup.getMember().getEmail(),
                        GroupNotification.from(updatedGroup, message));

                for (GroupInfo agreedParticipant : agreedParticipants){
                    sseService.sendGroupNotification(agreedParticipant.getMember().getEmail(),
                            GroupNotification.from(updatedGroup, message));
                }

                // 모일 수 있는 일정이 1개라면 자동으로 모임을 확정 짓고 모두에게 알림 전송
            } else if (recommendEvents.size() == 1){
                message = groupTitle + " 모임이 확정 되었습니다.";

                // Group 확정 처리
                updatedGroup.confirm();
                updatedGroup.setConfirmedDateTime(recommendEvents.get(0));
                groupRepository.save(updatedGroup);

                // 호스트도 알림 발송
                sseService.sendGroupNotification(updatedGroup.getMember().getEmail(),
                        GroupNotification.from(updatedGroup, message));

                for (GroupInfo agreedParticipant : agreedParticipants){
                    sseService.sendGroupNotification(agreedParticipant.getMember().getEmail(),
                            GroupNotification.from(updatedGroup, message));
                }
                // 추천 일정이 여러개라면 모임 확정 알림을 호스트 에게만 전송
            } else {
                message = groupTitle + " 모임을 확정해주세요";

                // 호스트한테만 알림 발송
                sseService.sendGroupNotification(updatedGroup.getMember().getEmail(),
                        GroupNotification.from(updatedGroup, message));
            }
        }

        return VoteResponse.from(savedGroupInfo, findParticipant);
    }

    private void sendAlarmForParticipants(GroupAlarm groupAlarm) {
        // 알림은 아직 참여 또는 거절을 선택하지 않은 상태이고, 존재하는 유저한테만 보내야 한다.
        List<GroupInfo> participants =
                groupInfoRepository.findByGroupAndIsAgreed(groupAlarm.getGroup(), "P");

        String message = "모임 참여 결정까지"
                + groupAlarm.getDeadlineAlarm()
                + groupAlarm.getGroupAlarmTimeType().name()
                + "남았습니다.";

        for (GroupInfo participant : participants) {
            sseService.sendGroupNotification(
                    participant.getMember().getEmail(),
                    GroupNotification.from(participant.getGroup(), message));
        }
        groupAlarm.sendCheck("Y");
    }

    // 모임 참여자 전원이 투표했는 지 확인하는 메서드
    private boolean checkVoteStatus(Group group) {
        List<GroupInfo> groupInfos = groupInfoRepository.findByGroup(group);
        for (GroupInfo groupInfo : groupInfos) {
            log.info("GroupInfo agreeYn 확인");
            if (groupInfo.getIsAgreed().equals("P")){
                return false;
            }
        }
        return true;
    }

    // 일정 추천 로직
    private List<LocalDateTime> recommendGroupSchedule(Group group){

        LocalDate expectStartDate = group.getExpectStartDate();
        LocalDate expectEndDate = group.getExpectEndDate();
        LocalTime expectStartTime = group.getExpectStartTime();
        LocalTime expectEndTime = group.getExpectEndTime();
        int runningTime = group.getRunningTime();


        // 각 사용자의 불가능한 슬롯을 나타내는 리스트
        List<LocalDateTime[]> allUnavailableSlots = new ArrayList<>();
        // 각 사용자의 불가능한 슬롯을 추가(시작일자, 종료일자)
//        데이터를 가져올때 start_date와 end_date 안에있는 모든 일정 데이터를 가져와 하나하나 넣어주기 밑에는 예시 데이터/ 무조건 첫번째가 일정 시작시간, 두번째가 일정 종료시간이어야 함

        // 수락을 누른 모든 게스트의 정보를 넣기
        List<GroupInfo> groupInfoList =  groupInfoRepository.findByGroupAndIsAgreed(group, "Y");

        // 모임에 수락한 Members
        List<Member> agreedMembers = new ArrayList<>();

        // 호스트 정보 넣기
        agreedMembers.add(group.getMember());

        // 모임 참여자 정보 넣기
        for (GroupInfo groupInfo : groupInfoList){
            agreedMembers.add(groupInfo.getMember());
        }

        LocalDateTime startOfMeetingRange = expectStartDate.atStartOfDay(); // 모임 시작 날짜의 00:00
        LocalDateTime endOfMeetingRange = expectEndDate.atTime(LocalTime.MAX); // 모임 종료 날짜의 23:59:59.999999999

        for (Member member: agreedMembers) {
            // 각각의 일정 리스트를 합침
            List<Event> memberEvents = eventRepository.findByMember(member);
            for (Event event : memberEvents) {
                // 개인 일정의 시작과 종료 시간
                LocalDateTime eventStart = event.getStartDateTime();
                LocalDateTime eventEnd = event.getEndDateTime();

                // 개인 일정이 모임의 날짜 범위에 걸쳐있는지 확인
                // 개인 일정의 시작 시간이 모임 범위 안에 있거나, 개인 일정의 종료 시간이 모임 범위 안에 있는 경우
                // 또는 개인 일정이 모임 범위를 포함하는 경우 (모임 시작 전에 시작하여 모임 종료 후에 끝나는 경우)
                if ((eventStart.isBefore(endOfMeetingRange) && eventStart.isAfter(startOfMeetingRange)) ||
                        (eventEnd.isAfter(startOfMeetingRange) && eventEnd.isBefore(endOfMeetingRange)) ||
                        (eventStart.isBefore(startOfMeetingRange) && eventEnd.isAfter(endOfMeetingRange))) {
                    allUnavailableSlots.add(new LocalDateTime[]{eventStart, eventEnd});
                }
            }
        }
        log.info( "모든 이벤트 "+ allUnavailableSlots.toString());
        // 불가능한 슬롯을 합치는 메소드임(시간이 겹치는거 합쳐주는거)
        List<LocalDateTime[]> fixAllUnavailableSlots = mergeOverlappingSlots(allUnavailableSlots);

        // limit가 보여주고 싶은 갯수
        return findMeetingStartTimes(
                expectStartDate,
                expectEndDate,
                expectStartTime,
                expectEndTime,
                fixAllUnavailableSlots,
                runningTime,
                3);
    }

    private List<LocalDateTime[]> mergeOverlappingSlots(List<LocalDateTime[]> slots) {
        log.info("불가능 리스트 알고리즘 시작");
//       //비어 있으면 빈 리스트로 반환
        if (slots.isEmpty()) return Collections.emptyList();

        // 빠른 시작시간으로 정렬
        slots.sort(Comparator.comparing(slot -> slot[0]));

        // 겹치는 슬롯 저장하는 리스트 merged
        List<LocalDateTime[]> merged = new ArrayList<>();
        // 저장할 첫번째 리스트를 current
        LocalDateTime[] current = slots.get(0);

        for (int i = 1; i < slots.size(); i++) {
            //
            if (current[1].isBefore(slots.get(i)[0])) {
                // No overlap
                merged.add(current);
                current = slots.get(i);
            } else {
                // Overlap, extend the current slot
                current[1] = current[1].isAfter(slots.get(i)[1]) ? current[1] : slots.get(i)[1];
            }
        }

        merged.add(current);
        for (int i = 0; i < merged.size(); i++) {
            log.info( "불가능 시간 리스트" +  Arrays.toString(merged.get(i)));
        }
        return merged;
    }

    // limit로 반환하고 싶은 추천 일정의 갯수 지정
    private List<LocalDateTime> findMeetingStartTimes(LocalDate startDate,
                                                      LocalDate endDate,
                                                      LocalTime dailyStartTime,
                                                      LocalTime dailyEndTime,
                                                      List<LocalDateTime[]> allUnavailableSlots,
                                                      int runningTime,
                                                      int limit) {
        List<LocalDateTime> availableStartTimes = new ArrayList<>();

        // 하루씩 더해가면서 도는거
        loopAll:
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            LocalDateTime startDateTime = LocalDateTime.of(date, dailyStartTime); // 현재 날짜의 시작 시간
            LocalDateTime endDateTime = LocalDateTime.of(date, dailyEndTime); // 현재 날짜의 마지막 시간

            // 현재 날짜의 시작 시간부터 종료 시간까지, 설정된 30분을 더해감
            while (!startDateTime.plusMinutes(runningTime).isAfter(endDateTime)) {
                LocalDateTime currentStart = startDateTime;
                LocalDateTime currentFinsh = startDateTime.plusMinutes(runningTime); // 현재 시간에서 러닝타임을 더해 끝나는 시간
                availableStartTimes.add(currentStart);
                // 불가능한 슬롯이랑 겹치는지 확인하는 로직
                for (int i = 0; i < allUnavailableSlots.size(); i++) {
//                    불가능한 시간 1개씩 담는거 temp
                    LocalDateTime[] temp = allUnavailableSlots.get(i);
//                    시간,종료시간이 불가능 시간 전이나 후에 있으면
//                    비교한는 불가능 시간의 앞에있던가 뒤에 있으면 true를 반환함
                    if(!(currentFinsh.isBefore(temp[0].plusMinutes(1)) || currentStart.isAfter(temp[1].minusMinutes(1)))){
                        availableStartTimes.remove(currentStart);
                        break;
                    }

                }
                startDateTime = startDateTime.plusMinutes(30);

                if(availableStartTimes.size() == limit){ // 3개면 모든걸 끝내기
                    break loopAll;
                }
            }
        }
        log.info("availableStartTimes = " + availableStartTimes);

        return availableStartTimes;
    }


    // 이메일로 회원 찾기
    private Member findMemberByEmail() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
    }

    private void validate(Member participant, Group group, GroupInfo groupInfo){

        // group과 groupInfo의 존재 및 일치 여부 검증
        if (!group.equals(groupInfo.getGroup())) {
            throw new GroupAndGroupInfoNotMatchException();
        }

        // GroupInfo의 Participants와 현재 로그인 투표하는 Participant 검증
        if (!groupInfo.getMember().getEmail().equals(participant.getEmail())) {
            throw new ParticipantInfoNotMatchException();
        }

        // 이미 투표했을 경우, 예외
        if (!groupInfo.getIsAgreed().equals("P")) {
            throw new AlreadyVotedException();
        }
    }
}
