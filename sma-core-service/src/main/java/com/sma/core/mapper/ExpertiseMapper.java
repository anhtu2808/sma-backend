package com.sma.core.mapper;

import com.sma.core.dto.request.expertise.ExpertiseRequest;
import com.sma.core.dto.response.expertise.ExpertiseResponse;
import com.sma.core.entity.JobExpertise;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ExpertiseMapper {
    @Mapping(target = "expertiseGroup", source = "expertiseGroup")
    ExpertiseResponse toResponse(JobExpertise entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "expertiseGroup", ignore = true)
    JobExpertise toEntity(ExpertiseRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    @Mapping(target = "expertiseGroup", ignore = true)
    void updateEntity(@MappingTarget JobExpertise entity, ExpertiseRequest request);
}
