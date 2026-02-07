package com.sma.core.service;

import com.sma.core.dto.request.expertise.ExpertiseRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.expertise.ExpertiseResponse;
import org.springframework.data.domain.Pageable;

public interface ExpertiseService {
    ExpertiseResponse create(ExpertiseRequest request);

    ExpertiseResponse update(Integer id, ExpertiseRequest request);

    void delete(Integer id);

    ExpertiseResponse getById(Integer id);

    PagingResponse<ExpertiseResponse> getAll(String name, Integer groupId, Pageable pageable);
}
