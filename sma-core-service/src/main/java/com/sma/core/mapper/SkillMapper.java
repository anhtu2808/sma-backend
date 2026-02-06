package com.sma.core.mapper;
import com.sma.core.dto.request.skill.SkillRequest;
import com.sma.core.dto.response.skill.SkillCateResponse;
import com.sma.core.dto.response.skill.SkillResponse;
import com.sma.core.entity.Skill;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    SkillResponse toResponse(Skill skill);


    @Mapping(target = "category", source = "category")
    SkillCateResponse toCateResponse(Skill skill);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "category", ignore = true)
    Skill toEntity(SkillRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "category", ignore = true)
    void updateSkill(@MappingTarget Skill skill, SkillRequest request);

}
