package com.sma.core.service;

import com.sma.core.dto.request.expertise.ExpertiseGroupRequest;
import com.sma.core.dto.response.expertise.ExpertiseGroupResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ExpertiseGroupService {

    ExpertiseGroupResponse create(ExpertiseGroupRequest request);
    ExpertiseGroupResponse update(Integer id, ExpertiseGroupRequest request);
    void delete(Integer id);
    Page<ExpertiseGroupResponse> getAll(String name, Pageable pageable);
    ExpertiseGroupResponse getById(Integer id);

}
