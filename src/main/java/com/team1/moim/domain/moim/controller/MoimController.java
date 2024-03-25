package com.team1.moim.domain.moim.controller;

import static com.team1.moim.global.response.SuccessMessage.CREATE_MOIM_SUCCESS;

import com.team1.moim.domain.member.service.MemberService;
import com.team1.moim.domain.moim.dto.request.MoimCreateRequest;
import com.team1.moim.domain.moim.dto.response.MoimDetailResponse;
import com.team1.moim.domain.moim.service.MoimService;
import com.team1.moim.global.response.SuccessResponse;
import java.net.URI;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/moim")
public class MoimController {

    private final MoimService moimService;
    private final MemberService memberService;

    @Autowired
    public MoimController(MoimService moimService, MemberService memberService) {
        this.moimService = moimService;
        this.memberService = memberService;
    }

    @PostMapping("/{moimId}/create")
    public ResponseEntity<SuccessResponse<MoimDetailResponse>> createMoim(@PathVariable Long moimId, MoimCreateRequest moimCreateRequest) {
        MoimDetailResponse response = moimService.createMoim(moimCreateRequest);
        return ResponseEntity.created(URI.create("/" + moimId + "/create"))
                .body(SuccessResponse.create(HttpStatus.CREATED.value(),
                        CREATE_MOIM_SUCCESS.getMessage(),
                        moimService.createMoim(moimCreateRequest)));
    }
}
