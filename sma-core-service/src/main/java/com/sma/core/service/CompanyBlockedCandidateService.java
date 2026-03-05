package com.sma.core.service;

import com.sma.core.dto.request.company.BlockCandidateRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.company.BlockedCandidateResponse;
import org.springframework.data.domain.Pageable;

public interface CompanyBlockedCandidateService {
    void blockCandidate(BlockCandidateRequest request);
    void unblockCandidate(Integer candidateId);
    PagingResponse<BlockedCandidateResponse> getBlacklist(Pageable pageable, String keyword);
    boolean isCandidateBlocked(Integer candidateId, Integer companyId);
}
