package com.sma.core.service;

import com.sma.core.dto.request.criteria.CriteriaRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.criteria.CriteriaResponse;
import com.sma.core.entity.Company;
import org.springframework.data.domain.Pageable;

public interface CriteriaService {

    PagingResponse<CriteriaResponse> getAll(String name, Pageable pageable);
    CriteriaResponse getById(Integer id);
    CriteriaResponse create(CriteriaRequest request);
    CriteriaResponse update(Integer id, CriteriaRequest request);
    void delete(Integer id);
    void initCriteria(Company company);
}
