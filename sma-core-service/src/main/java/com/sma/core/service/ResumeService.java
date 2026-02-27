package com.sma.core.service;

import com.sma.core.dto.request.resume.UpdateResumeRequest;
import com.sma.core.dto.request.resume.UploadResumeRequest;
import com.sma.core.dto.response.resume.ResumeDetailResponse;
import com.sma.core.dto.response.resume.ResumeResponse;
import com.sma.core.enums.ResumeType;

import java.util.List;

public interface ResumeService {
    List<ResumeResponse> getMyResumes(String keyword, ResumeType type);
    ResumeDetailResponse getResumeDetail(Integer resumeId);
    ResumeResponse uploadResume(UploadResumeRequest request);
    ResumeResponse parseResume(Integer resumeId);
    ResumeResponse updateResume(Integer resumeId, UpdateResumeRequest request);
    ResumeResponse cloneResume(Integer resumeId, ResumeType targetType);
    String getResumeStatus(Integer resumeId);
    String getResumeParseStatus(Integer resumeId);
    void deleteResume(Integer resumeId);
    ResumeResponse createResumeBuilder();
}
