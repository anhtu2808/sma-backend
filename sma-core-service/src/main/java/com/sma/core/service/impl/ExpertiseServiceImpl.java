package com.sma.core.service.impl;

import com.sma.core.dto.request.expertise.ExpertiseRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.expertise.ExpertiseResponse;
import com.sma.core.entity.JobExpertise;
import com.sma.core.entity.JobExpertiseGroup;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.ExpertiseMapper;
import com.sma.core.repository.ExpertiseGroupRepository;
import com.sma.core.repository.ExpertiseRepository;
import com.sma.core.service.ExpertiseService;
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
public class ExpertiseServiceImpl implements ExpertiseService {
    ExpertiseRepository repository;
    ExpertiseGroupRepository groupRepository;
    ExpertiseMapper mapper;

    @Override
    public ExpertiseResponse create(ExpertiseRequest request) {
        JobExpertiseGroup group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new AppException(ErrorCode.EXPERTISE_GROUP_NOT_FOUND));
        if (repository.existsByNameAndExpertiseGroupId(request.getName(), request.getGroupId())) {
            throw new AppException(ErrorCode.EXPERTISE_ALREADY_EXITED);
        }
        JobExpertise expertise = mapper.toEntity(request);
        expertise.setExpertiseGroup(group);
        return mapper.toResponse(repository.save(expertise));
    }

    @Override
    public PagingResponse<ExpertiseResponse> getAll(String name, Integer groupId, Pageable pageable) {
        Page<JobExpertise> expertises;
        boolean hasName = name != null && !name.trim().isEmpty();
        boolean hasGroupId = groupId != null;

        if (hasName && hasGroupId) {
            expertises = repository.findByNameContainingIgnoreCaseAndExpertiseGroup_Id(name, groupId, pageable);
        } else if (hasName) {
            expertises = repository.findByNameContainingIgnoreCaseOrExpertiseGroup_NameContainingIgnoreCase(name, name,
                    pageable);
        } else if (hasGroupId) {
            expertises = repository.findByExpertiseGroup_Id(groupId, pageable);
        } else {
            expertises = repository.findAll(pageable);
        }

        return PagingResponse.fromPage(expertises.map(mapper::toResponse));
    }

    @Override
    @Transactional(readOnly = true)
    public ExpertiseResponse getById(Integer id) {
        return repository.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
    }

    @Override
    public ExpertiseResponse update(Integer id, ExpertiseRequest request) {
        JobExpertise expertise = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));

        if (!expertise.getExpertiseGroup().getId().equals(request.getGroupId())) {
            JobExpertiseGroup newGroup = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new AppException(ErrorCode.EXPERTISE_GROUP_NOT_FOUND));
            expertise.setExpertiseGroup(newGroup);
        }

        mapper.updateEntity(expertise, request);
        return mapper.toResponse(repository.save(expertise));
    }

    @Override
    public void delete(Integer id) {
        JobExpertise expertise = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        if (expertise.getJobs() != null && !expertise.getJobs().isEmpty()) {
            throw new AppException(ErrorCode.CANT_DELETE_EXPERTISE_IN_USE);
        }
        repository.delete(expertise);
    }
}
