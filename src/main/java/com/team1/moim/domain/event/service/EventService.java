package com.team1.moim.domain.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team1.moim.domain.event.dto.request.AlarmRequest;
import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.RepeatRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.AlarmResponse;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.Alarm;
import com.team1.moim.domain.event.entity.AlarmType;
import com.team1.moim.domain.event.entity.Event;
import com.team1.moim.domain.event.entity.Matrix;
import com.team1.moim.domain.event.entity.Repeat;
import com.team1.moim.domain.event.entity.RepeatType;
import com.team1.moim.domain.event.entity.ToDoList;
import com.team1.moim.domain.event.exception.EventNotFoundException;
import com.team1.moim.domain.event.repository.AlarmRepository;
import com.team1.moim.domain.event.repository.EventRepository;
import com.team1.moim.domain.event.repository.RepeatRepository;
import com.team1.moim.domain.member.entity.Member;
import com.team1.moim.domain.member.exception.MemberNotFoundException;
import com.team1.moim.domain.member.exception.MemberNotMatchException;
import com.team1.moim.domain.member.repository.MemberRepository;
import com.team1.moim.domain.notification.NotificationType;
import com.team1.moim.domain.notification.dto.EventNotification;
import com.team1.moim.global.config.s3.S3Service;
import com.team1.moim.global.config.sse.service.SseService;
import jakarta.transaction.Transactional;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.chrono.ChronoLocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableAsync // 일정이 먼저 만들어지고 반복일정 메소드는 따로 만들어지도록 추가함
public class EventService {

    private static final String FILE_TYPE = "events";

    private final EventRepository eventRepository;
    private final MemberRepository memberRepository;
    private final RepeatRepository repeatRepository;
    private final AlarmRepository alarmRepository;
    private final S3Service s3Service;
    private final SseService sseService;
//    private final RedisService redisService;

    @Transactional
    public EventResponse create(MultipartFile file,
                                EventRequest eventRequest,
                                RepeatRequest repeatRequest,
                                List<ToDoListRequest> toDoListRequests,
                                List<AlarmRequest> alarmRequests) throws JsonProcessingException {
        Member member = findMemberByEmail();

        log.info("일정이 추가 됩니다.");
        Matrix matrix;
        if (eventRequest.getMatrix().equals("Q1")) {
            matrix = Matrix.Q1;
        } else if (eventRequest.getMatrix().equals("Q2")) {
            matrix = Matrix.Q2;
        } else if (eventRequest.getMatrix().equals("Q3")) {
            matrix = Matrix.Q3;
        } else {
            matrix = Matrix.Q4;
        }

        String fileUrl = null;
        if (file != null) {
            fileUrl = s3Service.uploadFile(FILE_TYPE, file);
        }

        // 신규 일정 생성
        Event newEvent = eventRequest.toEntity(matrix, fileUrl);
        newEvent.attachMember(member);

        log.info("신규 일정 초안 생성");

        // 일정에 To-Do 리스트 추가
        if (toDoListRequests != null) {
            for (ToDoListRequest toDoListRequest : toDoListRequests) {
                ToDoList toDoList = toDoListRequest.toEntity();
                toDoList.attachEvent(newEvent);
            }

            log.info("일정에 To-Do 리스트 등록 완료");
        }

        // 일정에 알림 추가
        if (alarmRequests != null && eventRequest.getAlarmYn().equals("Y")) {
            for (AlarmRequest alarmRequest : alarmRequests) {
                AlarmType alarmtype;
                if (alarmRequest.getAlarmType().equals("M")) {
                    alarmtype = AlarmType.M;
                } else if (alarmRequest.getAlarmType().equals("H")) {
                    alarmtype = AlarmType.H;
                } else {
                    alarmtype = AlarmType.D;
                }
                Alarm alarm = alarmRequest.toEntity(alarmtype);
                alarm.attachEvent(newEvent);
            }
            log.info("일정에 알림 등록 완료");
        }

        Event savedEvent = eventRepository.save(newEvent);
        log.info("DB에 일정 저장 완료");


        // 일정에 반복 일정 추가, 다음 반복 일정 생성하는 메소드 호출
        if (repeatRequest != null) {
            //부모 일정도 repeat_parent에 id 추가
            newEvent.setRepeatParent(newEvent.getId());

            RepeatType repeatType = switch (repeatRequest.getRepeatType()) {
                case "Y" -> RepeatType.Y;
                case "M" -> RepeatType.M;
                case "W" -> RepeatType.W;
                default -> RepeatType.D;
            };

            Repeat savedRepeat = repeatRepository.save(repeatRequest.toEntity(repeatType, savedEvent));
            addRepeatEvents(newEvent, savedRepeat);
            log.info("DB에 저장된 일정에 반복 일정 추가 완료");
        }

        return EventResponse.from(savedEvent);
    }

    private void addRepeatEvents(Event newEvent, Repeat repeat) {

        log.info("반복 일정 추가 함수 진입");
        LocalDateTime updatedStartDateTime = null; // 날짜가 커져서 반복 일정에 넣어줄 값
        LocalDateTime updatedEndDateTime = null;

        // 다음에 또 반복을 할지 판별하게 해주는 변수
        LocalDateTime nextStartDateTime = null;

        // 단위마다 추가하는 로직
        if (repeat.getRepeatType() == RepeatType.Y) {
            // 1년 후
            updatedStartDateTime = newEvent.getStartDateTime().plusYears(1); // 현재 반복일정에 들어갈 시작일자
            updatedEndDateTime = newEvent.getEndDateTime().plusYears(1);
            nextStartDateTime = updatedStartDateTime.plusYears(1); // 현재 반복일정 바로 뒤에 또 들어갈 일자를 미리 계산함
        } else if (repeat.getRepeatType() == RepeatType.M) {
            // 1달 후
            updatedStartDateTime = newEvent.getStartDateTime().plusMonths(1);
            updatedEndDateTime = newEvent.getEndDateTime().plusMonths(1);
            nextStartDateTime = updatedStartDateTime.plusMonths(1);
        } else if (repeat.getRepeatType() == RepeatType.W) {
            // 1주 후
            updatedStartDateTime = newEvent.getStartDateTime().plusWeeks(1);
            updatedEndDateTime = newEvent.getEndDateTime().plusWeeks(1);
            nextStartDateTime = updatedStartDateTime.plusWeeks(1);
        } else if (repeat.getRepeatType() == RepeatType.D) {
            // 1일 후
            updatedStartDateTime = newEvent.getStartDateTime().plusDays(1);
            updatedEndDateTime = newEvent.getEndDateTime().plusDays(1);
            nextStartDateTime = updatedStartDateTime.plusDays(1);
        }

        log.info("업데이트 된 시작 일정: {}", updatedStartDateTime);
        log.info("업데이트 된 종료 일정: {}", updatedEndDateTime);

        Long parentEventId;
        if (newEvent.getRepeatParent() != null) {
            parentEventId = newEvent.getRepeatParent();
        } else {
            parentEventId = newEvent.getId();
        }

        // 부모 일정에 대한 반복 일정 생성

        Event repeatEvent = new Event(newEvent);
        repeatEvent.changeData(updatedStartDateTime, updatedEndDateTime, parentEventId);

        log.info("반복 일정 초안 생성 완료");

        eventRepository.save(repeatEvent);

        log.info("생성된 반복 일정 DB에 저장 완료");

        // 현재 반복 일정 보다 1년 뒤(반복 일정 타입이 Y인 걸로 가정)인 nextStartDate가 반복 종료일보다 전이면 addRepeatEvents() 재귀 호출
        if (repeat.getRepeatEndDate().isAfter(ChronoLocalDate.from(nextStartDateTime.minusDays(1)))) {
            log.info("반복 종료일 이전이므로, addRepeatEvents 함수를 재호출합니다.");
            addRepeatEvents(repeatEvent, repeat);
        }
    }

    @Transactional
    public EventResponse update(Long eventId, MultipartFile file, EventRequest eventRequest) {
        Member member = findMemberByEmail();
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (!member.getId().equals(event.getMember().getId())) {
            throw new MemberNotMatchException();
        }
        String fileUrl = event.getFileUrl();
        if (file != null) {
            fileUrl = s3Service.uploadFile(FILE_TYPE, file);
        }
        Matrix matrix;
        if (eventRequest.getMatrix().equals("Q1")) matrix = Matrix.Q1;
        else if (eventRequest.getMatrix().equals("Q2")) matrix = Matrix.Q2;
        else if (eventRequest.getMatrix().equals("Q3")) matrix = Matrix.Q3;
        else matrix = Matrix.Q4;
        event.update(
                eventRequest.getTitle(),
                eventRequest.getMemo(),
                eventRequest.getStartDate(),
                eventRequest.getEndDate(),
                eventRequest.getPlace(),
                matrix,
                fileUrl);

        return EventResponse.from(event);
    }

    @Transactional
    public void delete(Long eventId) {
        log.info("delete");
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.delete();
    }

    @Transactional
    public void deleteRepeatEvents(Long eventId, String deleteType) {
        Member member = findMemberByEmail();
        Event event = eventRepository.findById(eventId).orElseThrow();
        if (!member.getId().equals(event.getMember().getId())) {
            throw new MemberNotMatchException();
        }

//        현재 이벤트
        log.info("deleteType = " + deleteType);
        // 현재 이벤트 지우기
        event.delete();

        Long repeatParentId = event.getRepeatParent();

        //같은 repeatParentId 를 가지고 있는 모든 이벤트 + 부모 포함
        List<Event> allEvent = new ArrayList<>(eventRepository.findByRepeatParentAndDeleteYn(repeatParentId, "N"));

        //부모 이벤트
        Event parentEvent = eventRepository.findById(repeatParentId).orElseThrow();

        //반복
        Repeat repeat = repeatRepository.findByEventId(repeatParentId);

        // 반복되는 일정 모두를 지움
        if (deleteType.equals("all")) {

            parentEvent.delete();

            for (Event value : allEvent) {
                Event repeatEvent = eventRepository.findById(value.getId()).orElseThrow();
                repeatEvent.delete();
            }

            // 지우고자 하는 반복 일정 이후를 모두 지움
        } else if (deleteType.equals("after")) {
            // 일정이 지워지고 난후 그 직전 날짜 구하기
            LocalDate lastestEndDate = LocalDate.parse("0001-01-01");
            // 현재일정 이후의 일정 구하기
            for (Event event1 : allEvent) {
                if (event1.getStartDateTime().isAfter(event.getStartDateTime()) ||
                        event1.getStartDateTime() == event.getStartDateTime()) { // 현재 일정보다 이후인것은 모두 삭제
                    event1.delete();
                } else if(lastestEndDate.isBefore(ChronoLocalDate.from(event1.getStartDateTime()))){ // 현재 일정보다 이전인 모든 일정은 모두 반복 종료일을 바꿔주기....
                    lastestEndDate = LocalDate.from(event1.getStartDateTime());
                }
            }
            // 모든 반복일정의 반복종료일자 변경하기


            repeat.changeEndDate(lastestEndDate);

            // 현재 한가지 일정만 지우기
        } else {
            // 만약 뒤의 일정이 없다면 모든 반복일정의 "반복 일정 종료일을"을 바로 직전으로 바꾸기
            LocalDateTime thisStartDate = event.getStartDateTime();
            LocalDateTime newLastDate = LocalDateTime.parse("0001-01-01T00:00:00"); // 새롭게 바뀔 반복 종료일
            for (Event event1 : allEvent) {
                if (event1.getStartDateTime().isAfter(thisStartDate) ||
                        event1.getStartDateTime() == event.getStartDateTime()) {
                    thisStartDate = event1.getStartDateTime(); // 가장 마지막 이벤트를 찾아서 thisStartDate에 넣어둠
                } else if (newLastDate.isBefore(event1.getStartDateTime())) {
                    newLastDate = event1.getStartDateTime();
                }
            }

            //현재 일정이 반복하는 일정 중 마지막 일정과 같다면 모든 반복데이터에 마지막 날짜를 바꿔줘야 함
            if (thisStartDate == event.getStartDateTime()) {
                repeat.changeEndDate(LocalDate.from(newLastDate));
            }


//            if(LocalDate.from(event.getStartDateTime()).equals(repeat.getRepeatEndDate())){
//                LocalDateTime newLastDate = LocalDateTime.parse("0001-01-01T00:00:00");
//                for(Event event1: allEvent){
//                    if(event1 != event){ // 자신을 제외한 상태로
//                        if(newLastDate.isBefore(event1.getStartDateTime())){
//                            newLastDate = event1.getStartDateTime();
//                        }
//                    }
//                }
//                repeat.changeEndDate(LocalDate.from(newLastDate));
//            }

        }
    }

    public List<EventResponse> matrixEvents(Matrix matrix) {
        Member member = findMemberByEmail();
        List<Event> events = eventRepository.findByMember(member);

        return events.stream()
                .filter(event -> event.getMatrix().equals(matrix))
                .filter(event -> event.getStartDateTime().isAfter(LocalDateTime.now()))
                .filter(event -> event.getStartDateTime().isBefore(LocalDateTime.now().plusMonths(1)))
                .map(EventResponse::from)
                .sorted(Comparator.comparing(EventResponse::getStartDate))
                .collect(Collectors.toList());
    }

    // 알림 전송 스케줄러
    @Scheduled(cron = "0 0/1 * * * *") // 매분마다 실행
    @Transactional
    public void eventSchedule() throws JsonProcessingException {
        // 삭제되지 않고, 알림 설정한 일정LiST
        List<Event> events = eventRepository.findByDeleteYnAndAlarmYn("N", "Y");
        for (Event event : events) {
            // 과거 일정은 알림 X
            if (event.getStartDateTime().isBefore(LocalDateTime.now())) continue;
            // 이미 전송한 알림 X
            List<Alarm> alarms = alarmRepository.findByEventAndSendYn(event, "N");
            for (Alarm alarm : alarms) {
                if (alarm.getAlarmtype() == AlarmType.D) {
                    // 지나간 알림은 전송 X
                    if (event.getStartDateTime().minusDays(alarm.getSetTime()).isBefore(LocalDateTime.now())) {
                        Member member = alarm.getEvent().getMember();
                        sseService.sendEventAlarm(member.getEmail(),
                                EventNotification.from(
                                        event.getId(),
                                        alarm,
                                        member,
                                        LocalDateTime.now(),
                                        NotificationType.EVENT));
                        alarm.sendCheck("Y");
                    }
                }
                if (alarm.getAlarmtype() == AlarmType.H) {
                    if (event.getStartDateTime().minusHours(alarm.getSetTime()).isBefore(LocalDateTime.now())) {
                        Member member = alarm.getEvent().getMember();
                        sseService.sendEventAlarm(member.getEmail(),
                                EventNotification.from(
                                        event.getId(),
                                        alarm,
                                        member,
                                        LocalDateTime.now(),
                                        NotificationType.EVENT));
                        alarm.sendCheck("Y");
                    }
                }
                if (alarm.getAlarmtype() == AlarmType.M) {
                    if (event.getStartDateTime().minusMinutes(alarm.getSetTime()).isBefore(LocalDateTime.now())) {
                        Member member = alarm.getEvent().getMember();
                        sseService.sendEventAlarm(member.getEmail(),
                                EventNotification.from(
                                        event.getId(),
                                        alarm,
                                        member,
                                        LocalDateTime.now(),
                                        NotificationType.EVENT));
                        alarm.sendCheck("Y");
                    }
                }
            }
        }
    }

    public List<EventResponse> getMonthly(int year, int month) {
        Member member = findMemberByEmail();
        log.info(member.getNickname() + "님 일정 조회");
        // 해당 월의 첫날과 마지막 날 구하기
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.with(TemporalAdjusters.lastDayOfMonth());
        // LocalDate를 LocalDateTime으로 변환 (시작 시간은 00:00, 종료 시간은 23:59로 설정)
        LocalDateTime start = startOfMonth.atStartOfDay();
        LocalDateTime end = endOfMonth.atTime(23, 59, 59);
        // 변환된 LocalDateTime 객체를 사용하여 쿼리 실행
        List<Event> events = eventRepository.findByMemberAndDateRange(member, start, end);
        // 조회된 일정이 없으면 에러
        if (events.isEmpty()) {
            throw new EventNotFoundException();
        }
        // EventResponse 조립
        List<EventResponse> eventResponses = new ArrayList<>();
        for (Event event : events) {
            log.info(event.getTitle());
            EventResponse eventResponse = EventResponse.from(event);
            eventResponses.add(eventResponse);
        }
        return eventResponses;
    }

    public List<EventResponse> getWeekly(int year, int week) {
        Member member = findMemberByEmail();
        log.info(member.getNickname() + "님 일정 조회");
        List<Event> events = eventRepository.findByMemberAndYearAndWeek(member, year, week);
        if (events.isEmpty()) {
            throw new EventNotFoundException();
        }
        List<EventResponse> eventResponses = new ArrayList<>();
        for (Event event : events) {
            log.info(event.getTitle());
            EventResponse eventResponse = EventResponse.from(event);
            eventResponses.add(eventResponse);
        }

        return eventResponses;
    }

    public List<EventResponse> getDaily(int year, int month, int day) {
        Member member = findMemberByEmail();
        log.info(member.getNickname() + "님 일정 조회");
        List<Event> events = eventRepository.findByMemberAndYearAndMonthAndDay(member, year, month, day);
        if (events.isEmpty()) throw new EventNotFoundException();
        List<EventResponse> eventResponses = new ArrayList<>();
        for (Event event : events) {
            log.info(event.getTitle());
            EventResponse eventResponse = EventResponse.from(event);
            eventResponses.add(eventResponse);
        }

        return eventResponses;
    }

    public EventResponse getEvent(Long eventId) {
        Member member = findMemberByEmail();
        Event event = eventRepository.findById(eventId).orElseThrow(EventNotFoundException::new);
        if (member != event.getMember()) {
            throw new MemberNotMatchException();
        }
        return EventResponse.from(event);
    }

    public List<EventResponse> searchEvent(String content) {
        Member member = findMemberByEmail();
        log.info(member.getNickname() + "님 일정 검색");

        List<Event> events = eventRepository.findByMemberAndTitleOrMemo(member, content);
        if (events.isEmpty()) throw new EventNotFoundException();
        LocalDateTime now = LocalDateTime.now();
        List<EventResponse> eventResponses = events.stream()
                .sorted(Comparator.comparing(event -> event.getStartDateTime().isBefore(now)
                        ? Duration.between(event.getStartDateTime(), now)
                        : Duration.between(now, event.getStartDateTime())))
                .map(EventResponse::from)
                .collect(Collectors.toList());

        for (EventResponse eventResponse : eventResponses) {
            log.info(eventResponse.getTitle());
        }

        return eventResponses;
    }
    // 메트릭스 수정
    @Transactional
    public void matrixUpdate (Long eventId, Matrix matrix){
        log.info("matrix update");
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.matrixUpdate(matrix);

    }

    // 이메일로 회원 찾기
    private Member findMemberByEmail() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return memberRepository.findByEmail(email).orElseThrow(MemberNotFoundException::new);
    }

    // 단일 일정의 알람을 조회
    public List<AlarmResponse> findAlarmByEventId(Long eventId) {
//        Member member = findMemberByEmail();
        List<AlarmResponse> alarmResponses = new ArrayList<>();
        for (Alarm alarm : alarmRepository.findByEventId(eventId)) {
            AlarmResponse alarmResponse = AlarmResponse.from(alarm);
            alarmResponses.add(alarmResponse);
        }
        return alarmResponses;
    }
}
