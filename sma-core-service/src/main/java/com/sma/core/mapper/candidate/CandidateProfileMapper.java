package com.sma.core.mapper.candidate;

import com.sma.core.dto.request.candidate.UpdateCandidateProfileRequest;
import com.sma.core.dto.response.candidate.CandidateProfileResponse;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.entity.Candidate;
import com.sma.core.entity.User;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface CandidateProfileMapper {
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatar", source = "user.avatar")
    CandidateProfileResponse toCandidateProfileResponse(Candidate candidate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "resumes", ignore = true)
    @Mapping(target = "applications", ignore = true)
    @Mapping(target = "profileCompleteness", ignore = true)
    void updateCandidateFromRequest(UpdateCandidateProfileRequest request, @MappingTarget Candidate candidate);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "lastLoginAt", ignore = true)
    @Mapping(target = "dateOfBirth", ignore = true)
    @Mapping(target = "gender", ignore = true)
    @Mapping(target = "role", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "recruiter", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "fullName", source = "fullName")
    @Mapping(target = "avatar", source = "avatar")
    void updateUserFromRequest(UpdateCandidateProfileRequest request, @MappingTarget User user);

    default CandidateProfileResponse mergeWithProfileResume(Candidate candidate, ResumeDetailResponse profileResume) {
        CandidateProfileResponse response = toCandidateProfileResponse(candidate);
        if (response == null) {
            response = new CandidateProfileResponse();
        }

        response.setPhone(null);

        if (profileResume != null) {
            response.setProfileResumeId(profileResume.getId());
//            response.setProfileResumeName(profileResume.getResumeName());
//            response.setProfileResumeFileName(profileResume.getFileName());
//            response.setProfileResumeUrl(profileResume.getResumeUrl());
            response.setResumeType(profileResume.getType());
            response.setResumeParseStatus(profileResume.getParseStatus());
            response.setSkills(profileResume.getSkills());
            response.setEducations(profileResume.getEducations());
            response.setExperiences(profileResume.getExperiences());
            response.setProjects(profileResume.getProjects());
            response.setCertifications(profileResume.getCertifications());
        }

        return response;
    }
}
