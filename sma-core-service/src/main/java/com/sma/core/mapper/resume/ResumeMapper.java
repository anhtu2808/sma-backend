package com.sma.core.mapper.resume;

import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.entity.Resume;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")

public interface ResumeMapper {
    Resume toEntity(UploadResumeRequest resume);

    @Mapping(target = "rootResumeId", source = "rootResume.id")
    ResumeResponse toResponse(Resume resume);
}
