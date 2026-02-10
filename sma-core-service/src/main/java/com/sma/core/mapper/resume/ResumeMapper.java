package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeRequest;
import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.entity.Resume;
import com.sma.core.enums.ResumeType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Builder;
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

    @BeanMapping(builder = @Builder(disableBuilder = true))
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resumeName", source = "source.resumeName")
    @Mapping(target = "fileName", source = "source.fileName")
    @Mapping(target = "rawText", source = "source.rawText")
    @Mapping(target = "addressInResume", source = "source.addressInResume")
    @Mapping(target = "phoneInResume", source = "source.phoneInResume")
    @Mapping(target = "emailInResume", source = "source.emailInResume")
    @Mapping(target = "githubLink", source = "source.githubLink")
    @Mapping(target = "linkedinLink", source = "source.linkedinLink")
    @Mapping(target = "portfolioLink", source = "source.portfolioLink")
    @Mapping(target = "fullName", source = "source.fullName")
    @Mapping(target = "avatar", source = "source.avatar")
    @Mapping(target = "resumeUrl", source = "source.resumeUrl")
    @Mapping(target = "type", source = "targetType")
    @Mapping(target = "rootResume", source = "rootResume")
    @Mapping(target = "status", source = "source.status")
    @Mapping(target = "parseStatus", source = "source.parseStatus")
    @Mapping(target = "language", source = "source.language")
    @Mapping(target = "isDefault", source = "source.isDefault")
    @Mapping(target = "candidate", source = "source.candidate")
    @Mapping(target = "isDeleted", source = "source.isDeleted")
    @Mapping(target = "skillGroups", ignore = true)
    @Mapping(target = "educations", ignore = true)
    @Mapping(target = "experiences", ignore = true)
    @Mapping(target = "projects", ignore = true)
    @Mapping(target = "certifications", ignore = true)
    @Mapping(target = "evaluations", ignore = true)
    Resume cloneEntity(Resume source, Resume rootResume, ResumeType targetType);
}
