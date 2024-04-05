package com.team1.moim.domain.event.controller;

import com.team1.moim.domain.event.dto.request.AlarmRequest;
import com.team1.moim.domain.event.dto.request.EventRequest;
import com.team1.moim.domain.event.dto.request.RepeatRequest;
import com.team1.moim.domain.event.dto.request.ToDoListRequest;
import com.team1.moim.domain.event.dto.response.EventResponse;
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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<ApiSuccessResponse<EventResponse>> create(HttpServletRequest servRequest,
                                                                    @Valid EventRequest request,
                                                                    @RequestPart(value = "toDoListRequests", required = false) List<ToDoListRequest> toDoListRequests,
                                                                    @RequestPart(value = "repeat", required = false) RepeatRequest repeatValue,
                                                                    @RequestPart(value = "alarmRequest", required = false) List<AlarmRequest> alarmRequest) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        eventService.create(request, toDoListRequests, repeatValue, alarmRequest)));
    }

    // 일정 수정
    @PreAuthorize("hasRole('ROLE_USER')")
    @PatchMapping("/{eventId}")
    public ResponseEntity<ApiSuccessResponse<EventResponse>> update(HttpServletRequest servRequest,
                                                                    @PathVariable(name = "eventId") Long eventId,
                                                                    @Valid EventRequest request) {
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        eventService.update(eventId, request)));
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
    public ResponseEntity<ApiSuccessResponse<String>> deleteRepeat(HttpServletRequest servRequest,
                                                                   @PathVariable(name = "eventId") Long eventId,
                                                                   @RequestParam("deleteType") String deleteType) {
        eventService.repeatDelete(eventId, deleteType);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        servRequest.getServletPath(),
                        ("삭제되었습니다.")));
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


}
