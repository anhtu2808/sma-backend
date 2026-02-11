package com.sma.core.service;

import com.sma.core.dto.request.application.ApplicationFilter;
import com.sma.core.dto.request.application.ApplicationRequest;
import com.sma.core.dto.response.application.ApplicationListResponse;
import com.sma.core.dto.response.application.ApplicationResponse;
import org.springframework.data.domain.Page;

public interface ApplicationService {
    ApplicationResponse applyToJob(ApplicationRequest request, Integer candidateId);
    Page<ApplicationListResponse> getApplicationsForRecruiter(ApplicationFilter filter);
}
