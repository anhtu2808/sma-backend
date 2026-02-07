package com.sma.core.service;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;

public interface RecruiterService {
    void registerRecruiter(RecruiterRegisterRequest request);
    RecruiterMyInfoResponse getMyInfo();
}
