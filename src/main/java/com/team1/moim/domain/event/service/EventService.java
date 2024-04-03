package com.team1.moim.domain.event.service;

import com.team1.moim.domain.event.dto.request.AlarmRequest;
import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.RepeatRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.*;
import com.team1.moim.domain.event.repository.AlarmRepository;
import com.team1.moim.domain.event.repository.EventRepository;
import com.team1.moim.domain.event.repository.RepeatRepository;
import com.team1.moim.domain.event.repository.ToDoListRepository;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.global.config.s3.S3Service;
import com.team1.moim.global.config.sse.NotificationResponse;
import com.team1.moim.global.config.sse.SseService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableAsync // 일정이 먼저 만들어지고 반복일정 메소드는 따로 만들어지도록 추가함
public class EventService {

    private static final String FILE_TYPE = "events";

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final ToDoListRepository toDoListRepository;
    private final RepeatRepository repeatRepository;
    private final AlarmRepository alarmRepository;
    private final S3Service s3Service;
    private final SseService sseService;

    public EventResponse create(EventRequest request, List<ToDoListRequest> toDoListRequests, RepeatRequest repeatValue, List<AlarmRequest> alarmRequests) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info(email);
        Member member = memberRepository.findByEmail(email).orElseThrow();

        log.info("일정이 추가 됩니다.");
        Matrix matrix;
        if (request.getMatrix().equals("Q1")) matrix = Matrix.Q1;
        else if (request.getMatrix().equals("Q2")) matrix = Matrix.Q2;
        else if (request.getMatrix().equals("Q3")) matrix = Matrix.Q3;
        else matrix = Matrix.Q4;
        String fileUrl = null;
        if (request.getFile() != null) {
            fileUrl = s3Service.uploadFile(FILE_TYPE, request.getFile());
        }
        Event event = EventRequest.toEntity(request.getTitle(), request.getMemo(), request.getStartDate(), request.getEndDate(), request.getPlace(), matrix, fileUrl, request.getRepeatParent(), member, request.getAlarmYn());
        eventRepository.save(event);
  
//      ToDoList 추가
        if (toDoListRequests != null) {
            for (ToDoListRequest toDoListRequest : toDoListRequests) {
                ToDoList toDoList = toDoListRequest.toEntity(toDoListRequest.getContents(), toDoListRequest.getIsChecked(), event);
                toDoListRepository.save(toDoList);
            }
        }
//        Repeat추가, 다음 반복일정 만드는 메소드 호출
        if (repeatValue != null) {

            RepeatType newRepeat;
            if (repeatValue.getReapetType().equals("Y")) newRepeat = RepeatType.Y;
            else if (request.getMatrix().equals("M")) newRepeat = RepeatType.M;
            else if (request.getMatrix().equals("W")) newRepeat = RepeatType.W;
            else newRepeat = RepeatType.D;

            log.info("반복일정이 추가됩니다.");
            Repeat repeatEntity = RepeatRequest.toEntity(newRepeat, repeatValue.getReapet_end_date(),event);
            repeatRepository.save(repeatEntity);
            repeatCreate(request, toDoListRequests, repeatValue, event.getId());
        }
//        Alarm 추가
        if(alarmRequests != null && request.getAlarmYn().equals("Y")) {
            for (AlarmRequest alarmRequest : alarmRequests) {
                AlarmType alarmtype;
                if (alarmRequest.getAlarmType().equals("M")) alarmtype = AlarmType.M;
                else if (alarmRequest.getAlarmType().equals("H")) alarmtype = AlarmType.H;
                else alarmtype = AlarmType.D;
                Alarm alarm = alarmRequest.toEntity(alarmtype, alarmRequest.getSetTime(), event);
                alarmRepository.save(alarm);
            }
        }
        return EventResponse.from(event);
    }

    @Async
    public EventResponse repeatCreate(EventRequest request, List<ToDoListRequest> toDoListRequests, RepeatRequest repeatValue, Long repeatParent) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow();
        Matrix matrix;
        if (request.getMatrix().equals("Q1")) matrix = Matrix.Q1;
        else if (request.getMatrix().equals("Q2")) matrix = Matrix.Q2;
        else if (request.getMatrix().equals("Q3")) matrix = Matrix.Q3;
        else matrix = Matrix.Q4;
        String fileUrl = null;
        if (request.getFile() != null) {
            fileUrl = s3Service.uploadFile(FILE_TYPE, request.getFile());
        }

        // 날짜형식으로 바꿔주는 형식
//        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        // startDate를 LocalDateTime으로 파싱
        LocalDateTime startDate = LocalDateTime.parse(request.getStartDate()); // 들어온값
        LocalDateTime endDate = LocalDateTime.parse(request.getEndDate());
        LocalDateTime calculatedStartDate = startDate; // 날짜가 커져서 반복일정에 넣어줄값
        LocalDateTime calculatedEndDate = endDate;

//        다음에 또 반복을 할지 판별하게 해주는 변수
        LocalDateTime nextStartDate = null;
        // 단위마다 추가하는 로직
        if (repeatValue.getReapetType().equals("Y")) {
            // 1년 후
            calculatedStartDate = startDate.plusYears(1); // 현재 반복일정에 들어갈 시작일자
            calculatedEndDate = startDate.plusYears(1);
            nextStartDate = calculatedStartDate.plusYears(1); // 현재 반복일정 바로 뒤에 또 들어갈 일자를 미리 계산함


        } else if (repeatValue.getReapetType().equals("M")) {
            // 1달 후
            calculatedStartDate = startDate.plusMonths(1);
            calculatedEndDate = startDate.plusMonths(1);
            nextStartDate = calculatedStartDate.plusMonths(1);
        } else if (repeatValue.getReapetType().equals("W")) {
            // 1주 후
            calculatedStartDate = startDate.plusWeeks(1);
            calculatedEndDate = startDate.plusWeeks(1);
            nextStartDate = calculatedStartDate.plusWeeks(1);
        } else if (repeatValue.getReapetType().equals("D")) {
            // 1일 후
            calculatedStartDate = startDate.plusDays(1);
            calculatedEndDate = startDate.plusDays(1);
            nextStartDate = calculatedStartDate.plusDays(1);
        }
        // LocalDateTime을 다시 String으로 변환
        String newStartDate = calculatedStartDate.toString();
        String newEndDate = calculatedEndDate.toString();
//        그리고 repeat도 넣어주기

        Event event = EventRequest.toEntity(request.getTitle(), request.getMemo(), newStartDate, newEndDate, request.getPlace(), matrix, fileUrl, repeatParent, member, request.getAlarmYn());
        eventRepository.save(event);

//        ToDoList 추가
        if (toDoListRequests != null) {
            for (ToDoListRequest toDoListRequest : toDoListRequests) {
                ToDoList toDoList = toDoListRequest.toEntity(toDoListRequest.getContents(), toDoListRequest.getIsChecked(), event);
                toDoListRepository.save(toDoList);
            }
        }

        RepeatType newRepeat;
        if (repeatValue.getReapetType().equals("Y")) newRepeat = RepeatType.Y;
        else if (request.getMatrix().equals("M")) newRepeat = RepeatType.M;
        else if (request.getMatrix().equals("W")) newRepeat = RepeatType.W;
        else newRepeat = RepeatType.D;
        
        Repeat repeatEntity = RepeatRequest.toEntity(newRepeat, repeatValue.getReapet_end_date(),event);
        repeatRepository.save(repeatEntity);

        // 만약 현재 반복일정 보다 1년뒤(반복일정 타입별이 Y인걸로 가정 하면)인 nextStartDate가 반복 종료일보다 전이면 RepeatCreate를 다시 호출한다.
        LocalDate repeatEndDate = LocalDate.parse(repeatValue.getReapet_end_date());
        if (repeatEndDate.isAfter(ChronoLocalDate.from(nextStartDate))){
            EventRequest newRequest = request.changeDateRequest(request, newStartDate, newEndDate);
            repeatCreate(newRequest, toDoListRequests, repeatValue, repeatParent);
        }
        
        return EventResponse.from(event);
    }


    @Transactional
    public EventResponse update(Long eventId, EventRequest request) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//        Member member = memberRepository.findByEmail(email).orElseThrow();
        Event event = eventRepository.findById(eventId).orElseThrow();
//        if(member.getId() != event.getMember().getId()) {
//            throw new AccessDeniedException("작성한 회원이 아닙니다.");
//        }
        String fileUrl = null;
        if (request.getFile() != null) {
            fileUrl = s3Service.uploadFile(FILE_TYPE, request.getFile());
        }
        Matrix matrix;
        if (request.getMatrix().equals("Q1")) matrix = Matrix.Q1;
        else if (request.getMatrix().equals("Q2")) matrix = Matrix.Q2;
        else if (request.getMatrix().equals("Q3")) matrix = Matrix.Q3;
        else matrix = Matrix.Q4;
        event.update(request.getTitle(), request.getMemo(), request.getStartDate(), request.getEndDate(), request.getPlace(), matrix, fileUrl);

        return EventResponse.from(event);

    }

    @Transactional
    public void delete(Long eventId) {
        log.info("delete");
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.delete();

    }

    @Transactional
    public void repeatDelete(Long eventId, String deleteType) {

//        현재 이벤트
        log.info("deleteType = " + deleteType);
        Event event = eventRepository.findById(eventId).orElseThrow();
        // 현재 이벤트 지우기
        event.delete();

        Long repeatParentId = event.getRepeatParent();
        // 모든 자식 이벤트
        if(event.getRepeatParent() == null) {
            repeatParentId = event.getId();
        }

        //같은 repeatParentId 를 가지고 있는 모든 이벤트
        List<Event> allEvent = new ArrayList<>(eventRepository.findByRepeatParent(repeatParentId));

        //부모 이벤트
        Event parentEvent = eventRepository.findById(repeatParentId).orElseThrow();



        // 반복되는 일정 모두를 지움
        if(deleteType.equals("all")){

            parentEvent.delete();

            for (int i = 0; i < allEvent.size(); i++) {
                Event repeatEvent = eventRepository.findById(allEvent.get(i).getId()).orElseThrow();
                repeatEvent.delete();
            }


            // 지우고자 하는 반복 일정 이후를 모두 지움
        } else if (deleteType.equals("after")) {

            // 일정이 지워지고 난후 그 직전 날짜 구하기
            LocalDate lastest_end_date = LocalDate.parse("0001-01-01");
            // 현재일정 이후의 일정 구하기
            for (int i = 0; i < allEvent.size(); i++) {
                Event event1 = allEvent.get(i);
                if (event1.getStartDate().isAfter(event.getStartDate())||event1.getStartDate()==event.getStartDate()){ // 현재 일정보다 이후인것은 모두 삭제
                    event1.delete();
                }else{ // 현재 일정보다 이전인 모든 일정은 모두 반복 종료일을 바꿔주기....
                    lastest_end_date = LocalDate.from(event1.getStartDate());
                }
            }
            // 모든 반복일정의 반복종료일자 변경하기
            //부모객체와 모든 자식객체의 반복 종료일 고치기
            Repeat parentRepeat = repeatRepository.findByEventId(repeatParentId);
            parentRepeat.changeEndDate(lastest_end_date);
            for (int i = 0; i < allEvent.size(); i++) {
                Repeat repeat = repeatRepository.findByEventId(allEvent.get(i).getId());
                repeat.changeEndDate(lastest_end_date);
            }

            // 현재 한가지 일정만 지우기
        }else{
            // 만약 뒤의 일정이 없다면 모든 반복일정의 "반복 일정 종료일을"을 바로 직전으로 바꾸기
            LocalDateTime lastDate = event.getStartDate();
            LocalDateTime newLastDate = LocalDateTime.parse("0001-01-01T00:00:00"); // 새롭게 바뀔 반복 종료일
            for (int i = 0; i < allEvent.size(); i++) {
                Event event1 = allEvent.get(i);
                if(event1.getStartDate().isAfter(event.getStartDate())||event1.getStartDate()==event.getStartDate()){
                    lastDate = event1.getStartDate();
                }else if(newLastDate.isBefore(event1.getStartDate())){
                    newLastDate = event1.getStartDate();
                }
            }

            //현재 일정이 반복하는 일정 중 마지막 일정과 같다면 모든 반복데이터에 마지막 날짜를 바꿔줘야 함
            if(lastDate == event.getStartDate()){
                for (int i = 0; i < allEvent.size(); i++) {
                    Repeat repeatTemp = repeatRepository.findByEventId(allEvent.get(i).getId());
                    repeatTemp.changeEndDate(LocalDate.from(newLastDate));

                }
                Repeat repeatParent = repeatRepository.findById(repeatParentId).orElseThrow();
                repeatParent.changeEndDate(LocalDate.from(newLastDate));
            }

        }
    }

    // 알림 전송 스케줄러
    @Transactional
    @Scheduled(cron = "0 0/1 * * * *") // 매분마다 실행
    public void eventSchedule() {
        List<Event> events = eventRepository.findByDeleteYnAndAlarmYn("N", "Y");
        for(Event event : events) {
            if(event.getStartDate().isBefore(LocalDateTime.now())) continue;
            List<Alarm> alarms = alarmRepository.findByEventAndSendYn(event, "N");
            for(Alarm alarm : alarms) {
                if(alarm.getAlarmtype() == AlarmType.D) {
                    if(event.getStartDate().minusDays(alarm.getSetTime()).isBefore(LocalDateTime.now())) {
                        Member member = alarm.getEvent().getMember();
                        sseService.sendEventAlarm(member.getEmail(), NotificationResponse.from(alarm, member, LocalDateTime.now()));
                        alarm.sendCheck("Y");
                    }
                } else if (alarm.getAlarmtype() == AlarmType.H) {
                    if (event.getStartDate().minusHours(alarm.getSetTime()).isBefore(LocalDateTime.now())) {
                        Member member = alarm.getEvent().getMember();
                        sseService.sendEventAlarm(member.getEmail(), NotificationResponse.from(alarm, member, LocalDateTime.now()));
                        alarm.sendCheck("Y");
                    }
                } else if (alarm.getAlarmtype() == AlarmType.M) {
                    if (event.getStartDate().minusMinutes(alarm.getSetTime()).isBefore(LocalDateTime.now())) {
                        Member member = alarm.getEvent().getMember();
                        sseService.sendEventAlarm(member.getEmail(), NotificationResponse.from(alarm, member, LocalDateTime.now()));
                        alarm.sendCheck("Y");
                    }
                }
            }
        }
    }
}
