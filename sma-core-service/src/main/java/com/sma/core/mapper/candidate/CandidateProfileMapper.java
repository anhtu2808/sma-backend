package com.sma.core.mapper.candidate;

import com.sma.core.dto.response.candidate.CandidateProfileResponse;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.entity.Candidate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CandidateProfileMapper {
    @Mapping(target = "fullName", source = "user.fullName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "avatar", source = "user.avatar")
    CandidateProfileResponse toCandidateProfileResponse(Candidate candidate);

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
