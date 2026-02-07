package com.sma.core.mapper.recruiter;

import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.dto.response.myinfo.UserMyInfoResponse;
import com.sma.core.entity.Recruiter;
import com.sma.core.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterMapper {
    @Mapping(target = "companyId", source = "company.id")
    RecruiterMyInfoResponse toRecruiterMyInfoResponse(Recruiter recruiter);
    UserMyInfoResponse toUserMyInfoResponse(User user);
}
