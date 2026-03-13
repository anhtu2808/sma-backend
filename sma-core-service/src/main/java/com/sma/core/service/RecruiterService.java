package com.sma.core.service;

import com.sma.core.dto.request.auth.RecruiterRegisterRequest;
import com.sma.core.dto.request.user.CreateRecruiterMemberRequest;
import com.sma.core.dto.request.user.UpdateRecruiterMemberRequest;
import com.sma.core.dto.request.user.UpdateRecruiterMemberStatusRequest;
import com.sma.core.dto.response.myinfo.RecruiterMyInfoResponse;
import com.sma.core.dto.response.recruiter.RecruiterMemberResponse;

import java.util.List;

public interface RecruiterService {
    void registerRecruiter(RecruiterRegisterRequest request);
    RecruiterMyInfoResponse getMyInfo();
    RecruiterMyInfoResponse createMember(CreateRecruiterMemberRequest request);
    List<RecruiterMemberResponse> getMembers();
    RecruiterMemberResponse updateMember(Integer recruiterId, UpdateRecruiterMemberRequest request);
    RecruiterMemberResponse updateMemberStatus(Integer recruiterId, UpdateRecruiterMemberStatusRequest request);
}
