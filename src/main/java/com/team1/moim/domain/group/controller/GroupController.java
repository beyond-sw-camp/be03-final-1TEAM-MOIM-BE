package com.team1.moim.domain.group.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.team1.moim.domain.group.dto.request.GroupAlarmRequest;
import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.request.GroupSearchRequest;
import com.team1.moim.domain.group.dto.response.FindConfirmedGroupResponse;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.dto.response.ListGroupResponse;
import com.team1.moim.domain.group.service.GroupService;
import com.team1.moim.domain.group.dto.response.VoteResponse;
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
@RequestMapping("/api/groups")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 모임 생성
    @PreAuthorize("hasRole('ROLE_USER')")
    @PostMapping("/create")
    public ResponseEntity<ApiSuccessResponse<GroupDetailResponse>> createGroup(
            HttpServletRequest httpServletRequest,
            @Valid GroupRequest groupRequest,
            @RequestPart(value = "groupInfoRequests", required = false) List<GroupInfoRequest> groupInfoRequests,
            @RequestPart(value = "groupAlarmRequests", required = false) List<GroupAlarmRequest> groupAlarmRequests) throws JsonProcessingException {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.create(groupRequest, groupInfoRequests, groupAlarmRequests)));
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
            Pageable pageable) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.findGroups(pageable)));
    }

    @PostMapping("/{groupId}/groupInfo/{groupInfoId}/notification")
    public ResponseEntity<ApiSuccessResponse<VoteResponse>> vote(HttpServletRequest httpServletRequest,
                                                                 @PathVariable("groupId") Long groupId,
                                                                 @PathVariable("groupInfoId") Long groupInfoId,
                                                                 @RequestParam("agreeYn") String agreeYn) throws JsonProcessingException {
        log.info("참여자 투표 API 시작");
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.vote(groupId, groupInfoId, agreeYn)));
    }
}
