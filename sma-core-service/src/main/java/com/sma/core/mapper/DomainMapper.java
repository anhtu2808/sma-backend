package com.sma.core.mapper;

import com.sma.core.dto.response.job.DomainResponse;
import com.sma.core.entity.Domain;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DomainMapper {

    DomainResponse toResponse(Domain domain);

}
