package com.team1.moim.domain.group.service;

import com.amazonaws.services.ec2.model.transform.EgressOnlyInternetGatewayStaxUnmarshaller;
import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.repository.EventRepository;
import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.request.GroupSearchRequest;
import com.team1.moim.domain.group.dto.request.GroupVotedRequest;
import com.team1.moim.domain.group.dto.response.*;
import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.group.exception.GroupInfoNotFoundException;
import com.team1.moim.domain.group.exception.GroupNotFoundException;
import com.team1.moim.domain.group.exception.ParticipantRequiredException;
import com.team1.moim.domain.group.repository.GroupInfoRepository;
import com.team1.moim.domain.group.repository.GroupRepository;
import com.team1.moim.domain.group.util.DateTimeFormatterUtil;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.s3.S3Service;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.ChronoLocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final S3Service s3Service;

    // 모임 생성하기
    @Transactional
    public GroupDetailResponse create(
            GroupRequest groupRequest,
            List<GroupInfoRequest> groupInfoRequests) {

        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // 호스트 조회
        Member host = memberRepository.findByEmail(email)
                .orElseThrow(MemberNotFoundException::new);

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

        return GroupDetailResponse.from(groupRepository.save(newGroup));
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

    @Transactional
    public void voted(GroupVotedRequest groupVotedRequest, Long groupId, String loginEmail) {
        // 현재 투표한 그룹
        Group group = groupRepository.findById(groupId).orElseThrow();

        // 해당 그룹인포 리스트
        List<GroupInfo> groupInfoList =  groupInfoRepository.findByGroup(group).orElseThrow(GroupInfoNotFoundException::new);

        // 현재 투표한 사람을 찾아서 바꾸기
        for (GroupInfo groupInfo : groupInfoList) {
            if(groupInfo.getMember().getEmail().equals(loginEmail)){
                groupInfo.vote(groupVotedRequest.getIsAgreed());
            }
        }

        // 게스트가 투표를 다했는지 확인, 알람보내기
        int votedParticipants = 0; // 현재 투표를 한 사람의 수
        for (GroupInfo groupInfo : groupInfoList) {
            if (!groupInfo.getIsAgreed().equals("P")) {
                log.info(groupInfo.getIsAgreed());
                votedParticipants++;
            }
        }
        log.info("votedParticipants" + votedParticipants);

        if(group.getParticipants() == votedParticipants){
            log.info("알고리즘 실행" + group.getId() +"번 그룹");
            // 시간 추천 알고리즘 메소드
            meetingScheduler(group);
            // 호스트에게 확정 시키라고 알람 가기

        }

    }
    //시간추천 알고리즘
    public List<LocalDateTime> meetingScheduler (Group group){

        LocalDate expect_start_date = group.getExpectStartDate();
        LocalDate expect_end_date = group.getExpectEndDate();
        LocalTime expect_start_time = group.getExpectStartTime();
        LocalTime expect_end_time = group.getExpectEndTime();
        int running_time = group.getRunningTime();


        // 각 사용자의 불가능한 슬롯을 나타내는 리스트
        List<LocalDateTime[]> allUnavailableSlots = new ArrayList<>();
        // 각 사용자의 불가능한 슬롯을 추가(시작일자, 종료일자)
//        데이터를 가져올때 start_date와 end_date 안에있는 모든 일정 데이터를 가져와 하나하나 넣어주기 밑에는 예시 데이터/ 무조건 첫번째가 일정 시작시간, 두번째가 일정 종료시간이어야 함

        List<GroupInfo> groupInfoList =  groupInfoRepository.findByGroup(group).orElseThrow(GroupInfoNotFoundException::new);

        // 수락을 누른 모든 게스트의 정보를 넣기
        List<Member> allAgreeMember = new ArrayList<>();
        for (GroupInfo groupInfo : groupInfoList){
            if(groupInfo.getIsAgreed().equals("Y")){
                allAgreeMember.add(groupInfo.getMember());
            }
        }
        // 호스트의 정보도 넣기
        allAgreeMember.add(group.getMember());

        for (Member member: allAgreeMember) {
            // 각각의 일정 리스트를 합침
            List<Event> memberEvent = eventRepository.findByMember(member);
            for (Event event: memberEvent){
                // 모임 시작날짜가 개인 일정 사이에 걸쳐 있을 수 있으니,
                // 개인일정의 종료 날짜가 모임의 시작 날짜보다 전이거나, 개인일정의 시작 날짜가 모임 종료날짜보다 뒤인것을 제외한 모든 것들을 불가능한 슬롯에 삽입
                if(event.getStartDate().toLocalDate().isAfter(expect_start_date) && event.getStartDate().toLocalDate().isBefore(expect_end_date)){
                    allUnavailableSlots.add(new LocalDateTime[]{event.getStartDate(), event.getEndDate()});
                    log.info("들어가는 이벤트 시작 시간 1  " + event.getStartDate());

                }else if (event.getEndDate().toLocalDate().isAfter(expect_start_date) && event.getEndDate().toLocalDate().isBefore(expect_end_date)){
                    log.info("들어가는 이벤트 시작 시간 2  " + event.getStartDate());
                }else {
                    log.info("expect_start_date " + expect_start_date);
                    log.info("event.getStartDate().toLocalDate() " + event.getStartDate().toLocalDate());
                    log.info("expect_end_date " + expect_end_date);
                    log.info("event.getEndDate().toLocalDate() " + event.getEndDate().toLocalDate());

                    log.info("안 들어가는 이벤트 시작 시간  " + event.getStartDate());
                }
            }
        }
        log.info( "모든 이벤트 "+ allUnavailableSlots.toString());
        // 불가능한 슬롯을 합치는 메소드임(시간이 겹치는거 합쳐주는거)
        List<LocalDateTime[]> fixAllUnavailableSlots = mergeOverlappingSlots(allUnavailableSlots);

        // limit가 보여주고 싶은 갯수
        return findMeetingStartTimes(expect_start_date, expect_end_date, expect_start_time, expect_end_time, fixAllUnavailableSlots, running_time, 3);

    }
    public static List<LocalDateTime[]> mergeOverlappingSlots(List<LocalDateTime[]> slots) {
        log.info("불가능 리스트 알고리즘 시작");
//       //비어있으면 빈리스트로 반환
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
    //    limit는 반환하고 싶은 갯수를 지정하는건데 나중에 혹쉬 갯수 더 보여주고 싶거나 적게 보여주고 싶으면 여기서 바꾸면 된다.
    public static List<LocalDateTime> findMeetingStartTimes(LocalDate startDate, LocalDate endDate, LocalTime dailyStartTime, LocalTime dailyEndTime, List<LocalDateTime[]> allUnavailableSlots, int runningTime, int limit) {
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


}
