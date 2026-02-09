package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UpdateResumeCertificationRequest;
import com.sma.core.dto.response.resume.ResumeCertificationDetailResponse;
import com.sma.core.entity.ResumeCertification;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ResumeCertificationMapper {

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "resume", ignore = true)
    void updateFromRequest(UpdateResumeCertificationRequest request, @MappingTarget ResumeCertification certification);

    ResumeCertificationDetailResponse toResponse(ResumeCertification certification);
}
