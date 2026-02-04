package com.sma.core.service;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.request.company.CompanyVerificationRequest;
import com.sma.core.enums.CompanyStatus;

public interface RecruiterService {
    void registerRecruiter(RecruiterRegisterRequest request);
}
