package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeExperienceRequest;
import com.sma.core.dto.response.resume.ResumeExperienceResponse;
import com.sma.core.entity.ResumeExperience;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = ResumeExperienceDetailMapper.class)
public interface ResumeExperienceMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    @Mapping(target = "details", ignore = true)
    void updateFromRequest(UpdateResumeExperienceRequest request, @MappingTarget ResumeExperience experience);

    ResumeExperienceResponse toResponse(ResumeExperience experience);
}
