package com.sma.core.mapper.candidate;

import com.sma.core.dto.response.candidate.CandidateInvitationResponse;
import com.sma.core.dto.response.myinfo.CandidateMyInfoResponse;
import com.sma.core.dto.response.myinfo.UserMyInfoResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.User;
import com.sma.core.mapper.UserMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {
        UserMapper.class
})
public interface CandidateMapper {
    CandidateMyInfoResponse toCandidateMyInfoResponse(Candidate candidate);
    UserMyInfoResponse toUserMyInfoResponse(User user);
    CandidateInvitationResponse toCandidateInvitationResponse(Candidate candidate);
}
