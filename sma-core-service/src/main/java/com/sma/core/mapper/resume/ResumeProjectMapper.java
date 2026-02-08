package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeProjectRequest;
import com.sma.core.dto.response.resume.ResumeProjectResponse;
import com.sma.core.entity.ResumeProject;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = ProjectSkillMapper.class)
public interface ResumeProjectMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "skills", ignore = true)
    void updateFromRequest(UpdateResumeProjectRequest request, @MappingTarget ResumeProject project);

    ResumeProjectResponse toResponse(ResumeProject project);
}
