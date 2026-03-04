package com.sma.core.service;

import com.sma.core.dto.request.invitation.CreateInvitationRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.invitation.InvitationResponse;

public interface InvitationService {

    PagingResponse<InvitationResponse> getMyInvitations(Integer candidateId, Integer size, Integer page);
    PagingResponse<InvitationResponse> getMyCompanyInvitations(Integer recruiterId, Integer size, Integer page);
    InvitationResponse createInvitation(CreateInvitationRequest request);
    InvitationResponse getInvitationById(Integer invitationId);

}
