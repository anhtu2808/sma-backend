package com.sma.core.mapper.candidate;

import com.sma.core.dto.response.myinfo.CandidateMyInfoResponse;
import com.sma.core.dto.response.myinfo.UserMyInfoResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CandidateMapper {
    CandidateMyInfoResponse toCandidateMyInfoResponse(Candidate candidate);
    UserMyInfoResponse toUserMyInfoResponse(User user);
}
