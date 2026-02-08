package com.sma.core.service;

import com.sma.core.dto.request.candidate.UpdateCandidateProfileRequest;
import com.sma.core.dto.response.candidate.CandidateProfileResponse;
import com.sma.core.dto.response.myinfo.CandidateMyInfoResponse;

public interface CandidateService {
    CandidateMyInfoResponse getMyInfo();

    CandidateProfileResponse getMyProfile();

    CandidateProfileResponse updateMyProfile(UpdateCandidateProfileRequest request);
}
