package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeExperienceDetailRequest;
import com.sma.core.dto.response.resume.ResumeExperienceDetailResponse;
import com.sma.core.entity.ResumeExperienceDetail;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", uses = ExperienceSkillMapper.class)
public interface ResumeExperienceDetailMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "experience", ignore = true)
    @Mapping(target = "skills", ignore = true)
    void updateFromRequest(UpdateResumeExperienceDetailRequest request, @MappingTarget ResumeExperienceDetail detail);

    ResumeExperienceDetailResponse toResponse(ResumeExperienceDetail detail);
}
