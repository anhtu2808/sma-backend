package com.sma.core.mapper;

import com.sma.core.dto.request.skill.SkillCategoryRequest;
import com.sma.core.dto.response.skill.SkillCateResponse;
import com.sma.core.dto.response.skill.SkillCategoryResponse;
import com.sma.core.dto.response.skill.SkillResponse;
import com.sma.core.entity.Skill;
import com.sma.core.entity.SkillCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillCategoryMapper {
    @Mapping(target = "category", source = "category")
    SkillCateResponse toCateResponse(Skill skill);

    SkillResponse toSimpleResponse(Skill skill);

    SkillCategoryResponse toCategoryResponse(SkillCategory category);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skills", ignore = true)
    SkillCategory toCategoryEntity(SkillCategoryRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "skills", ignore = true)
    void updateCategory(@MappingTarget SkillCategory category, SkillCategoryRequest request);

}
