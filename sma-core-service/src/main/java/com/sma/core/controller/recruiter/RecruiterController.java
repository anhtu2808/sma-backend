package com.sma.core.controller.recruiter;

import com.sma.core.dto.request.user.CreateRecruiterMemberRequest;
import com.sma.core.dto.request.user.UpdateRecruiterMemberRequest;
import com.sma.core.dto.request.user.UpdateRecruiterMemberStatusRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.dto.response.recruiter.RecruiterMemberResponse;
import com.sma.core.service.RecruiterService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping("/members")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<List<RecruiterMemberResponse>> getRecruiterMembers() {
        return ApiResponse.<List<RecruiterMemberResponse>>builder()
                .message("Get recruiter members successfully")
                .data(recruiterService.getMembers())
                .build();
    }

    @PutMapping("/members/{recruiterId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<RecruiterMemberResponse> updateRecruiterMember(
            @PathVariable Integer recruiterId,
            @RequestBody @Valid UpdateRecruiterMemberRequest request) {
        return ApiResponse.<RecruiterMemberResponse>builder()
                .message("Update recruiter member successfully")
                .data(recruiterService.updateMember(recruiterId, request))
                .build();
    }

    @PatchMapping("/members/{recruiterId}/status")
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<RecruiterMemberResponse> updateRecruiterMemberStatus(
            @PathVariable Integer recruiterId,
            @RequestBody @Valid UpdateRecruiterMemberStatusRequest request) {
        return ApiResponse.<RecruiterMemberResponse>builder()
                .message("Update recruiter member status successfully")
                .data(recruiterService.updateMemberStatus(recruiterId, request))
                .build();
    }
}
