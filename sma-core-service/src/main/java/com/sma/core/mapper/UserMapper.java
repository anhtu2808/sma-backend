package com.sma.core.mapper;

import com.sma.core.dto.response.user.CandidateDetailResponse;
import com.sma.core.dto.response.user.RecruiterDetailResponse;
import com.sma.core.dto.response.user.UserAdminResponse;
import com.sma.core.dto.response.user.UserDetailResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.Recruiter;
import com.sma.core.entity.User;
import com.sma.core.enums.Role;
import com.sma.core.mapper.job.JobMapper;
import com.sma.core.mapper.resume.ResumeMapper;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring", uses = {JobMapper.class, ResumeMapper.class})
public interface UserMapper {
    @Mapping(target = "mainActivity", ignore = true)
    @Mapping(target = "subActivity", ignore = true)
    @Mapping(target = "joinedAt", source = "lastLoginAt")
    UserAdminResponse toAdminResponse(User user);

    @AfterMapping
    default void mapPlatformActivity(User user, @MappingTarget UserAdminResponse.UserAdminResponseBuilder res) {
        if (user.getRole() == Role.CANDIDATE && user.getCandidate() != null) {
            int cvCount = user.getCandidate().getResumes() != null ? user.getCandidate().getResumes().size() : 0;
            int appCount = user.getCandidate().getApplications() != null ? user.getCandidate().getApplications().size() : 0;
            res.mainActivity(cvCount + " CVs Uploaded");
            res.subActivity(appCount + " Applications");
        }
        else if (user.getRole() == Role.RECRUITER && user.getRecruiter() != null) {
            Recruiter recruiter = user.getRecruiter();
            if (recruiter.getCompany() != null) {
                int jobCount = recruiter.getCompany().getJobs() != null ? recruiter.getCompany().getJobs().size() : 0;
                res.mainActivity(jobCount + " Job Posts");
                res.subActivity(recruiter.getCompany().getName());
            }
        }
    }


    @Mapping(target = "baseInfo", ignore = true)
    @Mapping(target = "recruiterDetail", source = "recruiter")
    @Mapping(target = "candidateDetail", source = "candidate")
    UserDetailResponse toUserDetailResponse(User user);

    @AfterMapping
    default void linkBaseInfo(User user, @MappingTarget UserDetailResponse.UserDetailResponseBuilder res) {
        res.baseInfo(toAdminResponse(user));
    }

    @Mapping(target = "resumes", source = "resumes")
    CandidateDetailResponse toCandidateDetailResponse(Candidate candidate);

    @Mapping(target = "companyName", source = "company.name")
    @Mapping(target = "companyLogo", source = "company.logo")
    @Mapping(target = "jobs", source = "company.jobs")
    RecruiterDetailResponse toRecruiterDetailResponse(Recruiter recruiter);
}
