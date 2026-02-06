package com.sma.core.mapper.job;

import com.sma.core.dto.request.domain.DomainRequest;
import com.sma.core.dto.response.job.DomainResponse;
import com.sma.core.entity.Domain;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface DomainMapper {

    DomainResponse toResponse(Domain domain);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    Domain toEntity(DomainRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "jobs", ignore = true)
    void updateDomain(@MappingTarget Domain domain, DomainRequest request);

}
