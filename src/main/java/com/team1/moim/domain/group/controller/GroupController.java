package com.team1.moim.domain.group.controller;

import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.response.FindConfirmedGroupResponse;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.service.GroupService;
import com.team1.moim.global.dto.ApiSuccessResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/group")
public class GroupController {

    private final GroupService groupService;

    @Autowired
    public GroupController(GroupService groupService) {
        this.groupService = groupService;
    }

    // 모임 생성
    @PostMapping("/create")
    public ResponseEntity<ApiSuccessResponse<GroupDetailResponse>> createGroup(
            HttpServletRequest httpServletRequest,
            @Valid GroupRequest groupRequest,
            @RequestPart(value = "groupInfoRequests", required = false) List<GroupInfoRequest> groupInfoRequests) {

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiSuccessResponse.of(
                        HttpStatus.OK,
                        httpServletRequest.getServletPath(),
                        groupService.create(groupRequest, groupInfoRequests)));
    }

    // 모임 삭제
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
    @GetMapping("/{groupId}")
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
}
