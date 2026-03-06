package com.sma.core.mapper;

import com.sma.core.dto.request.invitation.CreateInvitationRequest;
import com.sma.core.dto.response.invitation.InvitationResponse;
import com.sma.core.entity.Invitation;
import com.sma.core.mapper.candidate.CandidateMapper;
import com.sma.core.mapper.company.CompanyLocationMapper;
import com.sma.core.mapper.company.CompanyMapper;
import com.sma.core.mapper.job.JobMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        CandidateMapper.class,
        JobMapper.class,
        CompanyMapper.class
})
public interface InvitationMapper {

    InvitationResponse toInvitationResponse(Invitation invitation);
    Invitation toInvitation(CreateInvitationRequest request);

}
