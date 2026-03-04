package com.sma.core.mapper.resume;

import com.sma.core.dto.message.resume.parsed.*;
import com.sma.core.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParsedResumeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    ResumeEducation toEducation(ParsedResumeEducation source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    ResumeCertification toCertification(ParsedResumeCertification source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "details", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    ResumeExperience toExperience(ParsedResumeExperience source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experience", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    ResumeExperienceDetail toExperienceDetail(ParsedResumeExperienceDetail source);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "skills", ignore = true)
    @Mapping(target = "orderIndex", ignore = true)
    ResumeProject toProject(ParsedResumeProject source);
}
