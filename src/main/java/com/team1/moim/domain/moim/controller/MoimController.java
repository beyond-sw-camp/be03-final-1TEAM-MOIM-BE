package com.team1.moim.domain.moim.controller;

import static com.team1.moim.global.response.SuccessMessage.CREATE_MOIM_SUCCESS;
import static com.team1.moim.global.response.SuccessMessage.DELETE_MOIM_SUCCESS;

import com.team1.moim.domain.member.service.MemberService;
import com.team1.moim.domain.moim.dto.request.MoimCreateRequest;
import com.team1.moim.domain.moim.dto.response.FindPendingMoimResponse;
import com.team1.moim.domain.moim.dto.response.MoimDetailResponse;
import com.team1.moim.domain.moim.service.MoimService;
import com.team1.moim.global.response.SuccessResponse;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/moim")
public class MoimController {

    private final MoimService moimService;

    @Autowired
    public MoimController(MoimService moimService) {
        this.moimService = moimService;
    }

    // 모임 생성
    @PostMapping("/create")
//    public ResponseEntity<SuccessResponse<MoimDetailResponse>> createMoim(
//            @RequestBody @Valid MoimCreateRequest moimCreateRequest) {
//        return ResponseEntity.created(URI.create("/create"))
//                .body(SuccessResponse.create(HttpStatus.CREATED.value(),
//                        CREATE_MOIM_SUCCESS.getMessage(),
//                        moimService.createMoim(moimCreateRequest)));
//    }
    public ResponseEntity<MoimDetailResponse> createMoim(@Valid MoimCreateRequest moimCreateRequest) {
        return ResponseEntity.ok().body(moimService.createMoim(moimCreateRequest));
    }

    // 모임 삭제
    @DeleteMapping("/{moimId}/delete")
    public ResponseEntity<SuccessResponse<Void>> deleteMoim(@PathVariable Long moimId) {
        moimService.deleteMoim(moimId);
        return ResponseEntity.ok(SuccessResponse.delete(HttpStatus.OK.value(), DELETE_MOIM_SUCCESS.getMessage()));
    }

    // 모임 조회(일정 확정 전)
    @GetMapping("/{moimId}")
    public ResponseEntity<FindPendingMoimResponse> findPendingMoim(@PathVariable Long moimId) {
        return ResponseEntity.ok().body(moimService.findPendingMoim(moimId));
    }
}
