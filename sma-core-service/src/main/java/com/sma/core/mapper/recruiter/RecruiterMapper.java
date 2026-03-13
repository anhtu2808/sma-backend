package com.sma.core.mapper.recruiter;

import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.dto.response.myinfo.UserMyInfoResponse;
import com.sma.core.dto.response.recruiter.RecruiterMemberResponse;
import com.sma.core.entity.Recruiter;
import com.sma.core.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RecruiterMapper {
    @Mapping(target = "companyId", source = "company.id")
    RecruiterMyInfoResponse toRecruiterMyInfoResponse(Recruiter recruiter);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatar", source = "user.avatar")
    @Mapping(target = "gender", source = "user.gender")
    @Mapping(target = "status", source = "user.status")
    @Mapping(target = "lastLoginAt", source = "user.lastLoginAt")
    RecruiterMemberResponse toRecruiterMemberResponse(Recruiter recruiter);

    UserMyInfoResponse toUserMyInfoResponse(User user);
}
