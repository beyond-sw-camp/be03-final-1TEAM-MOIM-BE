package com.team1.moim.domain.group.controller;

import com.team1.moim.domain.group.dto.request.*;
import com.team1.moim.domain.group.dto.response.FindConfirmedGroupResponse;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.dto.response.ListGroupResponse;
import com.team1.moim.domain.group.service.GroupService;
import com.team1.moim.global.config.sse.service.SseService;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Slf4j
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;
    private final SseService sseService;

    @Autowired
    public GroupController(GroupService groupService, SseService sseService) {
        this.groupService = groupService;
        this.sseService = sseService;
    }

    // 모임 생성
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<ApiSuccessResponse<GroupDetailResponse>> createGroup(
            HttpServletRequest httpServletRequest,
            @Valid GroupRequest groupRequest,
            @RequestPart(value = "groupInfoRequests", required = false) List<GroupInfoRequest> groupInfoRequests,
            @RequestPart(value = "alarmRequest", required = false) List<GroupCreateAlarmRequest> groupCreateAlarmRequests) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.create(groupRequest, groupInfoRequests, groupCreateAlarmRequests)));
    }

    // 모임 삭제
    @PreAuthorize("hasRole('ROLE_USER')")
    @DeleteMapping("/delete/{groupId}")
    public ResponseEntity<ApiSuccessResponse<String>> deleteGroup(
            HttpServletRequest httpServletRequest,
            @PathVariable Long groupId) {

        groupService.delete(groupId);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        (groupId + "번 모임이 삭제되었습니다.")));
    }

    // 모임 조회(일정 확정 전)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/pending/{groupId}")
    public ResponseEntity<ApiSuccessResponse<FindPendingGroupResponse>> findPendingGroup(
            HttpServletRequest httpServletRequest,
            @PathVariable Long groupId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.findPendingGroup(groupId)));
    }

    // 모임 조회(일정 확정 후)
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/confirmed/{groupId}")
    public ResponseEntity<ApiSuccessResponse<FindConfirmedGroupResponse>> findConfirmedGroup(
            HttpServletRequest httpServletRequest,
            @PathVariable Long groupId) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.findConfirmedGroup(groupId)));
    }

    // 모임 리스트 조회
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/groups")
    public ResponseEntity<ApiSuccessResponse<List<ListGroupResponse>>> findAllGroups(
            HttpServletRequest httpServletRequest,
            GroupSearchRequest groupSearchRequest,
            Pageable pageable
    ) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.findGroups(groupSearchRequest, pageable, email)));
    }

//    @PreAuthorize("hasRole('ROLE_USER')")
//    @PostMapping("/voted/{groupId}")
//    public ResponseEntity<ApiSuccessResponse<String>> vote(
//            HttpServletRequest httpServletRequest,
//            @Valid GroupVotedRequest groupVotedRequest,
//            @PathVariable Long groupId) {
//        String email = SecurityContextHolder.getContext().getAuthentication().getName();
//
//        groupService.voted(groupVotedRequest, groupId, email);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(ApiSuccessResponse.of(
//                        HttpStatus.OK,
//                        httpServletRequest.getServletPath(),
//                        ("투표가 성공적으로 완료되었습니다.")));
//    }

}
