package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeSkillRequest;
import com.sma.core.dto.response.resume.ResumeSkillDetailResponse;
import com.sma.core.entity.ResumeSkill;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ResumeSkillMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skillGroup", ignore = true)
    @Mapping(target = "skill", ignore = true)
    void updateFromRequest(UpdateResumeSkillRequest request, @MappingTarget ResumeSkill resumeSkill);

    @Mapping(target = "skillGroupId", source = "skillGroup.id")
    @Mapping(target = "skillGroupName", source = "skillGroup.name")
    @Mapping(target = "skillId", source = "skill.id")
    @Mapping(target = "skillName", source = "skill.name")
    @Mapping(target = "skillDescription", source = "skill.description")
    @Mapping(target = "skillCategoryId", source = "skill.category.id")
    @Mapping(target = "skillCategoryName", source = "skill.category.name")
    ResumeSkillDetailResponse toResponse(ResumeSkill resumeSkill);
}
