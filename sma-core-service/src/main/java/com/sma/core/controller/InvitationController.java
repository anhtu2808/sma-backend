package com.sma.core.controller;

import com.sma.core.dto.request.invitation.CreateInvitationRequest;
import com.sma.core.dto.response.ApiResponse;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.invitation.InvitationResponse;
import com.sma.core.enums.Role;
import com.sma.core.service.InvitationService;
import com.sma.core.utils.JwtTokenProvider;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

@RestController
@RequestMapping("/v1/invitations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InvitationController {

    InvitationService invitationService;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ApiResponse<InvitationResponse> createInvitation(@RequestBody CreateInvitationRequest request){
        return ApiResponse.<InvitationResponse>builder()
                .message("Create invitation successfully")
                .data(invitationService.createInvitation(request))
                .build();
    }

    @GetMapping("/mine")
    @PreAuthorize("hasAnyRole('RECRUITER', 'CANDIDATE')")
    public ApiResponse<PagingResponse<InvitationResponse>> mineInvitation(@RequestParam Integer page, @RequestParam Integer size){
        PagingResponse<InvitationResponse> responses;
        boolean isCandidate = Objects.equals(JwtTokenProvider.getCurrentRole(), Role.CANDIDATE);
        if (isCandidate) {
            responses = invitationService.getMyInvitations(JwtTokenProvider.getCurrentCandidateId(), size, page);
        } else {
            responses = invitationService.getMyCompanyInvitations(JwtTokenProvider.getCurrentRecruiterId(), size, page);
        }
        return ApiResponse.<PagingResponse<InvitationResponse>>builder()
                .message(isCandidate ?
                        "Get my invitations successfully" :
                        "Get my company invitations successfully")
                .data(responses)
                .build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECRUITER', 'CANDIDATE', 'ADMIN')")
    public ApiResponse<InvitationResponse> getInvitation(@PathVariable Integer id){
        return ApiResponse.<InvitationResponse>builder()
                .message("Get invitation successfully")
                .data(invitationService.getInvitationById(id))
                .build();
    }

}
