package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.ExperienceSkillRequest;
import com.sma.core.dto.response.resume.ExperienceSkillResponse;
import com.sma.core.entity.ExperienceSkill;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExperienceSkillMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "detail", ignore = true)
    @Mapping(target = "skill", ignore = true)
    void updateFromRequest(ExperienceSkillRequest request, @MappingTarget ExperienceSkill experienceSkill);

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    ExperienceSkillResponse toResponse(ExperienceSkill experienceSkill);
}
