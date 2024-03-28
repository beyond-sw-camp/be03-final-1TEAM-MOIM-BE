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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.format.DateTimeFormatter;
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

    public EventResponse create(EventRequest request, List<ToDoListRequest> toDoListRequests, RepeatRequest repeatValue, List<AlarmRequest> alarmRequests) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info(email);
        Member member = memberRepository.findByEmail(email).orElseThrow();
        System.out.println("일정이 추가 됩니다.");
        System.out.println("repeat = " + repeatValue);
        Matrix matrix;
        if (request.getMatrix().equals("Q1")) matrix = Matrix.Q1;
        else if (request.getMatrix().equals("Q2")) matrix = Matrix.Q2;
        else if (request.getMatrix().equals("Q3")) matrix = Matrix.Q3;
        else matrix = Matrix.Q4;
        String fileUrl = null;
        if (request.getFile() != null) {
            fileUrl = s3Service.uploadFile(FILE_TYPE, request.getFile());
        }
        Event event = EventRequest.toEntity(request.getTitle(), request.getMemo(), request.getStartDate(), request.getEndDate(), request.getPlace(), matrix, fileUrl, request.getRepeatYn(), member, request.getAlarmYn());
        eventRepository.save(event);
//        ToDoList 추가
        if (toDoListRequests != null) {
            for (ToDoListRequest toDoListRequest : toDoListRequests) {
                ToDoList toDoList = toDoListRequest.toEntity(toDoListRequest.getContents(), toDoListRequest.getIsChecked(), event);
                toDoListRepository.save(toDoList);
            }
        }
//        Repeat추가, 다음 반복일정 만드는 메소드 호출
        if (repeatValue != null) {

            Repeat_type newRepeat;
            if (repeatValue.getReapetType().equals("Y")) newRepeat = Repeat_type.Y;
            else if (request.getMatrix().equals("M")) newRepeat = Repeat_type.M;
            else if (request.getMatrix().equals("W")) newRepeat = Repeat_type.W;
            else newRepeat = Repeat_type.D;

            System.out.println("반복일정이 추가됩니다.");
            Repeat repeatEntity = RepeatRequest.toEntity(newRepeat, repeatValue.getReapet_end_date(),event);
            repeatRepository.save(repeatEntity);
            RepeatCreate(request, toDoListRequests, repeatValue);
        }
//        Alarm 추가
        if(alarmRequests != null && request.getAlarmYn().equals("Y")) {
            for (AlarmRequest alarmRequest : alarmRequests) {
                Alarmtype alarmtype;
                if (alarmRequest.getAlarmType().equals("M")) alarmtype = Alarmtype.M;
                else if (alarmRequest.getAlarmType().equals("H")) alarmtype = Alarmtype.H;
                else alarmtype = Alarmtype.D;
                Alarm alarm = alarmRequest.toEntity(alarmtype, alarmRequest.getSetTime(), event);
                alarmRepository.save(alarm);
            }
        }

        return EventResponse.from(event);
    }


    @Async
    public EventResponse RepeatCreate(EventRequest request, List<ToDoListRequest> toDoListRequests, RepeatRequest repeatValue) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Member member = memberRepository.findByEmail(email).orElseThrow();
        System.out.println("메소드에 들어옴");
        System.out.println("repeatValue = " + repeatValue);
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
            System.out.println("nextStartDate = " + nextStartDate);

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

        Event event = EventRequest.toEntity(request.getTitle(), request.getMemo(), newStartDate, newEndDate, request.getPlace(), matrix, fileUrl, request.getRepeatYn(), member, request.getAlarmYn());
        eventRepository.save(event);

//        ToDoList 추가
        if (toDoListRequests != null) {
            for (ToDoListRequest toDoListRequest : toDoListRequests) {
                ToDoList toDoList = toDoListRequest.toEntity(toDoListRequest.getContents(), toDoListRequest.getIsChecked(), event);
                toDoListRepository.save(toDoList);
            }
        }

        // 만약 현재 반복일정 보다 1년뒤(반복일정 타입별이 Y인걸로 가정 하면)인 nextStartDate가 반복 종료일보다 전이면 RepeatCreate를 다시 호출한다.
        LocalDate repeatEndDate = LocalDate.parse(repeatValue.getReapet_end_date());
        if (repeatEndDate.isAfter(ChronoLocalDate.from(nextStartDate))){
            EventRequest newRequest = request.changeDateRequest(request, newStartDate, newEndDate);
            System.out.println("반복 한번 더");
            System.out.println("newRequest = " + newRequest);
            RepeatCreate(newRequest, toDoListRequests, repeatValue);
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
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.delete();
    }
}
