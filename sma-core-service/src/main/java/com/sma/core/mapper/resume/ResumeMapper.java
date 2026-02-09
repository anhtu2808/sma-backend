package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeRequest;
import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.entity.Resume;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")

public interface ResumeMapper {
    Resume toEntity(UploadResumeRequest resume);

    @Mapping(target = "rootResumeId", source = "rootResume.id")
    ResumeResponse toResponse(Resume resume);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "rawText", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "rootResume", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "parseStatus", ignore = true)
    @Mapping(target = "candidate", ignore = true)
    @Mapping(target = "skillGroups", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "certifications", ignore = true)
    @Mapping(target = "evaluations", ignore = true)
    void updateFromRequest(UpdateResumeRequest request, @MappingTarget Resume resume);
}
