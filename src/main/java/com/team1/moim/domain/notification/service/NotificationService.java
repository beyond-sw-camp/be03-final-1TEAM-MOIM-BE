package com.team1.moim.domain.notification.service;

import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.repository.EventRepository;
import com.team1.moim.domain.group.entity.Group;
import com.team1.moim.domain.group.entity.GroupInfo;
import com.team1.moim.domain.group.exception.AlreadyVotedException;
import com.team1.moim.domain.group.exception.ParticipantInfoNotMatchException;
import com.team1.moim.domain.group.repository.GroupInfoRepository;
import com.team1.moim.domain.group.repository.GroupRepository;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.GroupInfoNotFoundException;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.domain.notification.dto.response.VoteResponse;
import com.team1.moim.global.config.redis.RedisService;
import com.team1.moim.global.config.sse.dto.GroupNotification;
import com.team1.moim.global.config.sse.dto.NotificationResponse;
import com.team1.moim.global.config.sse.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {

    private final GroupRepository groupRepository;
    private final GroupInfoRepository groupInfoRepository;
    private final MemberRepository memberRepository;
    private final EventRepository eventRepository;
    private final SseService sseService;
    private final RedisService redisService;

    @Transactional
    public VoteResponse vote(Long groupInfoId, String agreeYn){
        String participantEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        Member findParticipant =
                memberRepository.findByEmail(participantEmail).orElseThrow(MemberNotFoundException::new);

        GroupInfo findGroupInfo =
                groupInfoRepository.findById(groupInfoId).orElseThrow(GroupInfoNotFoundException::new);

        // GroupInfo의 Participants와 현재 로그인 투표하는 Participant 검증
        if (!findGroupInfo.getMember().getEmail().equals(findParticipant.getEmail())) {
            throw new ParticipantInfoNotMatchException();
        }

        // 이미 투표했을 경우, 예외
        if (!findGroupInfo.getIsAgreed().equals("P")) {
            throw new AlreadyVotedException();
        }

        findGroupInfo.vote(agreeYn);

        // 변경 사항(동의 여부) 반영
        GroupInfo savedGroupInfo = groupInfoRepository.save(findGroupInfo);

        Group group = savedGroupInfo.getGroup();

        if (checkVoteStatus(savedGroupInfo.getGroup())){
            log.info("모임 참여자 전원 투표 완료!");
            List<LocalDateTime> recommendEvents = meetingScheduler(savedGroupInfo.getGroup());
            log.info("추천 일정: " + recommendEvents);
            List<GroupInfo> agreedParticipants =
                    groupInfoRepository.findByGroupAndIsAgreed(savedGroupInfo.getGroup(), "Y");

            // 모일 수 있는 시간이 없다면, 모두에게 모일 수 없다는 알림 발송
            if (recommendEvents.isEmpty()){
                String groupTitle = savedGroupInfo.getGroup().getTitle();
                String message = groupTitle + " 모임이 취소되었습니다.";

                // Group 확정 및 삭제 처리
                group.confirm();
                group.delete();

                groupRepository.save(group);

                // 호스트도 알림 발송
                sseService.sendGroupNotification(group.getMember().getEmail(),
                        GroupNotification.from(group, message));

                for (GroupInfo agreedParticipant : agreedParticipants){
                    sseService.sendGroupNotification(agreedParticipant.getMember().getEmail(),
                            GroupNotification.from(group, message));
                }

                // 모일 수 있는 일정이 1개라면 자동으로 모임을 확정 짓고 모두에게 알림 전송
            } else if (recommendEvents.size() == 1){
                String groupTitle = savedGroupInfo.getGroup().getTitle();
                String message = groupTitle + " 모임이 확정 되었습니다.";

                // Group 확정 처리
                group.confirm();
                group.setConfirmedDateTime(recommendEvents.get(0));
                groupRepository.save(group);

                // 호스트도 알림 발송
                sseService.sendGroupNotification(group.getMember().getEmail(),
                        GroupNotification.from(group, message));

                for (GroupInfo agreedParticipant : agreedParticipants){
                    sseService.sendGroupNotification(agreedParticipant.getMember().getEmail(),
                            GroupNotification.from(group, message));
                }
                // 추천 일정이 여러개라면 모임 확정 알림을 호스트 에게만 전송
            } else {
                String groupTitle = savedGroupInfo.getGroup().getTitle();
                String message = groupTitle + " 모임을 확정해주세요";

                // 호스트한테만 알림 발송
                sseService.sendGroupNotification(group.getMember().getEmail(),
                        GroupNotification.from(group, message));
            }
        }

        return VoteResponse.from(savedGroupInfo, findParticipant);
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

    // 시간추천 알고리즘
    private List<LocalDateTime> meetingScheduler(Group group){

        LocalDate expect_start_date = group.getExpectStartDate();
        LocalDate expect_end_date = group.getExpectEndDate();
        LocalTime expect_start_time = group.getExpectStartTime();
        LocalTime expect_end_time = group.getExpectEndTime();
        int running_time = group.getRunningTime();


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

        LocalDateTime startOfMeetingRange = expect_start_date.atStartOfDay(); // 모임 시작 날짜의 00:00
        LocalDateTime endOfMeetingRange = expect_end_date.atTime(LocalTime.MAX); // 모임 종료 날짜의 23:59:59.999999999

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
                expect_start_date,
                expect_end_date,
                expect_start_time,
                expect_end_time,
                fixAllUnavailableSlots,
                running_time,
                3);
    }
    private List<LocalDateTime[]> mergeOverlappingSlots(List<LocalDateTime[]> slots) {
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
    private List<LocalDateTime> findMeetingStartTimes(LocalDate startDate, LocalDate endDate, LocalTime dailyStartTime, LocalTime dailyEndTime, List<LocalDateTime[]> allUnavailableSlots, int runningTime, int limit) {
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

    public List<NotificationResponse> getAlarms(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(MemberNotFoundException::new);
        String key = member.getEmail();
        List<NotificationResponse> alarms = redisService.getList(key);
        return alarms;
    }
}
