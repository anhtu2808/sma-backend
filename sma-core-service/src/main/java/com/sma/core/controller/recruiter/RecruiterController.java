package com.sma.core.controller.recruiter;

import com.sma.core.dto.request.user.CreateRecruiterMemberRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/recruiter")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RecruiterController {

    RecruiterService recruiterService;

    @GetMapping("/me")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<RecruiterMyInfoResponse> getMyInfo() {
        return ApiResponse.<RecruiterMyInfoResponse>builder()
                .message("Get recruiter my info successfully")
                .data(recruiterService.getMyInfo())
                .build();
    }

    @PostMapping("/member")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<RecruiterMyInfoResponse> createRecruiterMember(@RequestBody @Valid CreateRecruiterMemberRequest request){
        return ApiResponse.<RecruiterMyInfoResponse>builder()
                .message("Create recruiter member successfully")
                .data(recruiterService.createMember(request))
                .build();
    }
}
