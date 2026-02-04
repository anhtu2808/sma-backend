package com.sma.core.mapper;

import com.sma.core.dto.response.skill.SkillResponse;
import com.sma.core.entity.Skill;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface SkillMapper {

    SkillResponse toResponse(Skill skill);

}
