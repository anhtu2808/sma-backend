package com.sma.core.service.impl;

import com.sma.core.dto.request.expertise.ExpertiseGroupRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.expertise.ExpertiseGroupResponse;
import com.sma.core.entity.JobExpertiseGroup;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.ExpertiseGroupMapper;
import com.sma.core.repository.ExpertiseGroupRepository;
import com.sma.core.service.ExpertiseGroupService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class ExpertiseGroupServiceImp implements ExpertiseGroupService {
    ExpertiseGroupRepository repository;
    ExpertiseGroupMapper mapper;

    @Override
    public ExpertiseGroupResponse create(ExpertiseGroupRequest request) {
        if (repository.existsByName(request.getName()))
            throw new AppException(ErrorCode.GROUP_ALREADY_EXITED);

        JobExpertiseGroup group = mapper.toEntity(request);
        return mapper.toResponse(repository.save(group));
    }

    public PagingResponse<ExpertiseGroupResponse> getAll(String name, Pageable pageable) {
        Page<JobExpertiseGroup> groups;
        if (name != null && !name.trim().isEmpty()) {
            groups = repository.findByNameContainingIgnoreCase(name, pageable);
        } else {
            groups = repository.findAll(pageable);
        }
        return PagingResponse.fromPage(groups.map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpertiseGroupResponse getById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    @Override
    public ExpertiseGroupResponse update(Integer id, ExpertiseGroupRequest request) {
        JobExpertiseGroup group = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        mapper.updateEntity(group, request);
        return mapper.toResponse(repository.save(group));
    }

    @Override
    public void delete(Integer id) {
        if (!repository.existsById(id)) {
            throw new AppException(ErrorCode.NOT_FOUND);
        }
        try {
            repository.deleteById(id);
            repository.flush();
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.EXPERTISE_GROUP_IN_USE);
        }
    }
}
