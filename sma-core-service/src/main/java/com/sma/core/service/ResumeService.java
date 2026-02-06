package com.sma.core.service;

import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeResponse;

public interface ResumeService {
    ResumeResponse uploadResume(UploadResumeRequest request);
    ResumeResponse reparseResume(Integer resumeId);
    String getResumeStatus(Integer resumeId);
}
