package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateResumeCertificationRequest;
import com.sma.core.dto.response.resume.ResumeCertificationDetailResponse;

public interface ResumeCertificationService {
    ResumeCertificationDetailResponse create(Integer resumeId, UpdateResumeCertificationRequest request);

    ResumeCertificationDetailResponse update(Integer resumeId, Integer certificationId, UpdateResumeCertificationRequest request);
}
