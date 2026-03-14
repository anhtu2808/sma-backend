package com.sma.core.mapper;

import com.sma.core.dto.response.job.ProposedCVResponse;
import com.sma.core.entity.ProposedResume;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProposedResumeMapper {

    @Mapping(target = "jobId", source = "job.id")
    @Mapping(target = "resumeId", source = "resume.id")
    @Mapping(target = "candidateId", source = "resume.candidate.id")
    @Mapping(target = "fullName", source = "resume.candidate.user.fullName")
    @Mapping(target = "gender", source = "resume.candidate.user.gender")
    @Mapping(target = "jobTitle", source = "resume.candidate.jobTitle")
    @Mapping(target = "address", source = "resume.candidate.address")
    ProposedCVResponse toProposeCVResponse(ProposedResume proposedResume);

}
