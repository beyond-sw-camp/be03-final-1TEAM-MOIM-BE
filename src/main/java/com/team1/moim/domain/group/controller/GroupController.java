package com.team1.moim.domain.group.controller;

import static com.team1.moim.global.response.SuccessMessage.DELETE_GROUP_SUCCESS;

import com.team1.moim.domain.group.dto.request.GroupInfoRequest;
import com.team1.moim.domain.group.dto.request.GroupRequest;
import com.team1.moim.domain.group.dto.response.FindPendingGroupResponse;
import com.team1.moim.domain.group.dto.response.GroupDetailResponse;
import com.team1.moim.domain.group.service.GroupService;
import com.team1.moim.global.response.SuccessResponse;
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
    public ResponseEntity<GroupDetailResponse> createGroup(
            @Valid GroupRequest groupRequest,
            @RequestPart(value = "groupInfoRequests", required = false) List<GroupInfoRequest> groupInfoRequests) {

        return ResponseEntity.ok().body(groupService.create(groupRequest, groupInfoRequests));
    }

    // 모임 삭제
    @DeleteMapping("/{groupId}/delete")
    public ResponseEntity<SuccessResponse<Void>> deleteGroup(@PathVariable Long groupId) {
        groupService.delete(groupId);
        return ResponseEntity.ok(SuccessResponse.delete(HttpStatus.OK.value(), DELETE_GROUP_SUCCESS.getMessage()));
    }

    // 모임 조회(일정 확정 전)
    @GetMapping("/{groupId}")
    public ResponseEntity<FindPendingGroupResponse> findPendingGroup(@PathVariable Long groupId) {
        return ResponseEntity.ok().body(groupService.findPendingGroup(groupId));
    }
}
