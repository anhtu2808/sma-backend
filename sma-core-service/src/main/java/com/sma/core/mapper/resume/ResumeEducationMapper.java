package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeEducationRequest;
import com.sma.core.dto.response.resume.ResumeEducationDetailResponse;
import com.sma.core.entity.ResumeEducation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ResumeEducationMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateFromRequest(UpdateResumeEducationRequest request, @MappingTarget ResumeEducation education);

    ResumeEducationDetailResponse toResponse(ResumeEducation education);
}
