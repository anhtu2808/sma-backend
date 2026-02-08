package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateProjectSkillRequest;
import com.sma.core.dto.response.resume.ProjectSkillResponse;
import com.sma.core.entity.ProjectSkill;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProjectSkillMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "project", ignore = true)
    @Mapping(target = "skill", ignore = true)
    void updateFromRequest(UpdateProjectSkillRequest request, @MappingTarget ProjectSkill projectSkill);

    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    ProjectSkillResponse toResponse(ProjectSkill projectSkill);
}
