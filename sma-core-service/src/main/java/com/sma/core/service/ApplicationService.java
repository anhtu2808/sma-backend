package com.sma.core.service;

import com.sma.core.dto.request.application.ApplicationRequest;
import com.sma.core.dto.response.application.ApplicationResponse;

public interface ApplicationService {
    ApplicationResponse applyToJob(ApplicationRequest request, Integer candidateId);
}
