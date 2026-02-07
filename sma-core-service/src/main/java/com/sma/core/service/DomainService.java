package com.sma.core.service;

import com.sma.core.dto.request.domain.DomainRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.job.DomainResponse;
import org.springframework.data.domain.Pageable;

public interface DomainService {
    DomainResponse createDomain(DomainRequest request);

    PagingResponse<DomainResponse> getAllDomains(String query, Pageable pageable);

    DomainResponse updateDomain(Integer id, DomainRequest request);

    void deleteDomain(Integer id);

    DomainResponse getDomainById(Integer id);
}
