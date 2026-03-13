package com.sma.core.service;

import com.sma.core.dto.request.application.ApplicationFilter;
import com.sma.core.dto.request.application.ApplicationRequest;
import com.sma.core.dto.response.application.ApplicationDetailResponse;
import com.sma.core.dto.response.application.ApplicationExportResponse;
import com.sma.core.dto.response.application.ApplicationListResponse;
import com.sma.core.dto.response.application.ApplicationResponse;
import com.sma.core.enums.ApplicationStatus;
import com.sma.core.enums.ExportType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface ApplicationService {
    ApplicationResponse applyToJob(ApplicationRequest request, Integer candidateId);
    Page<ApplicationListResponse> getApplicationsForRecruiter(ApplicationFilter filter);
    ApplicationDetailResponse getApplicationDetail(Integer applicationId);
    void updateStatus(Integer applicationId, ApplicationStatus status, String rejectReason);
    List<ApplicationExportResponse> getShortlistedForExport(Integer jobId, ExportType type);
}
