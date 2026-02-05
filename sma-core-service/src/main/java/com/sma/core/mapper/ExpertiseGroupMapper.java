package com.sma.core.mapper;

import com.sma.core.dto.request.expertise.ExpertiseGroupRequest;
import com.sma.core.dto.response.expertise.ExpertiseGroupResponse;
import com.sma.core.entity.JobExpertiseGroup;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExpertiseGroupMapper {
    ExpertiseGroupResponse toResponse(JobExpertiseGroup entity);

    @Mapping(target = "id", ignore = true)
    JobExpertiseGroup toEntity(ExpertiseGroupRequest request);

    @Mapping(target = "id", ignore = true)
    void updateEntity(@MappingTarget JobExpertiseGroup entity, ExpertiseGroupRequest request);
}
