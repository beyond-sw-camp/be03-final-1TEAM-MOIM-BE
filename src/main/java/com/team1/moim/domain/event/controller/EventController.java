package com.team1.moim.domain.event.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team1.moim.domain.event.dto.request.AlarmRequest;
import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.RepeatRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.AlarmResponse;
import com.team1.moim.domain.event.dto.response.EventResponse;
import com.team1.moim.domain.event.entity.Matrix;
import com.team1.moim.domain.event.service.EventService;
import com.team1.moim.domain.event.service.PublicHoliyDayAPI;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;
    private final PublicHoliyDayAPI publicHoliyDayAPI;

    @Autowired
    public EventController(EventService eventService, PublicHoliyDayAPI publicHoliyDayAPI) {
        this.eventService = eventService;
        this.publicHoliyDayAPI = publicHoliyDayAPI;
    }

    // 일정 등록
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<EventResponse>> create(
            HttpServletRequest servRequest,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "eventRequest") @Valid EventRequest eventRequest,
            @RequestPart(value = "repeatRequest", required = false) @Valid RepeatRequest repeatRequest,
            @RequestPart(value = "toDoListRequests", required = false) List<ToDoListRequest> toDoListRequests,
            @RequestPart(value = "alarmRequests", required = false) List<AlarmRequest> alarmRequests
    ) throws JsonProcessingException {
        log.info("일정 등록 API 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        eventService.create(file, eventRequest, repeatRequest, toDoListRequests, alarmRequests)));
    }

    // 일정 수정
    @PreAuthorize("hasRole('ROLE_USER')")
    @PatchMapping("/{eventId}")
    public ResponseEntity<ApiSuccessResponse<EventResponse>> update(
            HttpServletRequest servRequest,
            @PathVariable(name = "eventId") Long eventId,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "eventRequest") @Valid EventRequest eventRequest) {
        System.out.println("heeer");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        eventService.update(eventId, file, eventRequest)));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @PatchMapping("/matrixUpdate/{eventId}/{matrix}")
    public ResponseEntity<ApiSuccessResponse<String>> matrixUpdate(
            HttpServletRequest servRequest,
            @PathVariable(name = "eventId") Long eventId,
            @PathVariable(name = "matrix") Matrix matrix) {
        eventService.matrixUpdate(eventId,matrix);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        ("메트릭스가 변경되었습니다.")));
    }

    // 일정 삭제
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/{eventId}")
    public ResponseEntity<ApiSuccessResponse<String>> delete(HttpServletRequest servRequest,
                                                             @PathVariable(name = "eventId") Long eventId) {
        eventService.delete(eventId);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        ("삭제되었습니다.")));
    }

    //   반복일정의 삭제
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/repeat/{eventId}")
    public ResponseEntity<ApiSuccessResponse<String>> deleteRepeatEvents(HttpServletRequest servRequest,
                                                                         @PathVariable(name = "eventId") Long eventId,
                                                                         @RequestParam("deleteType") String deleteType) {
        eventService.deleteRepeatEvents(eventId, deleteType);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        ("삭제되었습니다.")));
    }
    //   메트릭스 일정 조회 api
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/matrix/{matrix}")
    public ResponseEntity<ApiSuccessResponse<List<EventResponse>>> matrixEvents(
            HttpServletRequest servRequest,
            @PathVariable(name = "matrix")Matrix matrix) {

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        eventService.matrixEvents(matrix)));
    }

    @PostMapping("/getHoliday")
    public ResponseEntity<ArrayList<HashMap<String, Object>>> holidayInfoApi(String year, String month) {

        log.info("year = " + year);
        log.info("month = " + month);

        ArrayList<HashMap<String, Object>> responseHolidayArr = new ArrayList<>();

        try {
            Map<String, Object> holidayMap = PublicHoliyDayAPI.holidayInfoAPI(year, month);
            Map<String, Object> response = (Map<String, Object>) holidayMap.get("response");
            Map<String, Object> body = (Map<String, Object>) response.get("body");

            int totalCount = (int) body.get("totalCount");

            //공휴일이 없는 경우
            if(totalCount <= 0) {
                log.info("공휴일 없음");
                log.info("body = " + body);
            }
            // 공휴일이 하루 있는 경우
            if(totalCount == 1) {
                HashMap<String, Object> items = (HashMap<String, Object>) body.get("items");
                HashMap<String, Object> item = (HashMap<String, Object>) items.get("item");
                responseHolidayArr.add(item);
                log.info("item = " + item);
            }
            //공휴일이 1일 이상 있는 경우
            if(totalCount > 1) {
                HashMap<String, Object> items = (HashMap<String, Object>) body.get("items");
                ArrayList<HashMap<String, Object>> item = (ArrayList<HashMap<String,Object>>) items.get("item");
                for(HashMap<String, Object> itemMap : item) {
                    log.info("itemMap = " + itemMap);
                    responseHolidayArr.add(itemMap);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ResponseEntity<>(responseHolidayArr, HttpStatus.OK);
    }

//    월별 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/monthly/{year}/{month}")
    public ResponseEntity<ApiSuccessResponse<List<EventResponse>>> getMonthly(HttpServletRequest httpServletRequest,
                                                                              @PathVariable("year") int year,
                                                                              @PathVariable("month") int month) {
        log.info("월별 조회 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        eventService.getMonthly(year, month)));
    }

//    주별 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/weekly/{year}/{week}")
    public ResponseEntity<ApiSuccessResponse<List<EventResponse>>> getWeekly(HttpServletRequest httpServletRequest,
                                                                              @PathVariable("year") int year,
                                                                              @PathVariable("week") int week) {
        log.info("주별 조회 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        eventService.getWeekly(year, week)));
    }

//    일별 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/daily/{year}/{month}/{day}")
    public ResponseEntity<ApiSuccessResponse<List<EventResponse>>> getDaily(HttpServletRequest httpServletRequest,
                                                                             @PathVariable("year") int year,
                                                                             @PathVariable("month") int month,
                                                                             @PathVariable("day") int day) {
        log.info("일별 조회 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        eventService.getDaily(year, month, day)));
    }

//    일정 상세 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/{eventId}")
    public ResponseEntity<ApiSuccessResponse<EventResponse>> getEvent(HttpServletRequest httpServletRequest,
                                                                            @PathVariable("eventId") Long eventId) {
        log.info("상세 조회 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        eventService.getEvent(eventId)));
    }

    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/search/{content}")
    public ResponseEntity<ApiSuccessResponse<List<EventResponse>>> searchEvent(HttpServletRequest httpServletRequest,
                                                                      @PathVariable("content") String content) {
        log.info("검색 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        eventService.searchEvent(content)));
    }

    // 단일 일정의 알림 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/search/alarm/{event_id}")
    public ResponseEntity<ApiSuccessResponse<List<AlarmResponse>>> searchAlarm(
            HttpServletRequest httpServletRequest, @PathVariable("event_id") Long eventId) {
        log.info("단일 일정의 알림 조회 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        eventService.findAlarmByEventId(eventId)));
    }
}
