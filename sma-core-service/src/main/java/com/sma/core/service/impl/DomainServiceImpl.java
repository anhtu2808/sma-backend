package com.sma.core.service.impl;

import com.sma.core.dto.request.domain.DomainRequest;
import com.sma.core.dto.response.job.DomainResponse;
import com.sma.core.entity.Domain;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.job.DomainMapper;
import com.sma.core.repository.DomainRepository;
import com.sma.core.service.DomainService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class DomainServiceImpl implements DomainService {

    DomainRepository domainRepository;
    DomainMapper domainMapper;

    // CREATE
    @Override
    public DomainResponse createDomain(DomainRequest request) {
        if (domainRepository.existsByName(request.getName())) {
            throw new AppException(ErrorCode.DOMAIN_ALREADY_EXISTED);
        }
        Domain domain = domainMapper.toEntity(request);
        return domainMapper.toResponse(domainRepository.save(domain));
    }

    // READ (Get All with Pagination & Search)
    @Transactional(readOnly = true)
    @Override
    public Page<DomainResponse> getAllDomains(String query, Pageable pageable) {
        Page<Domain> domains = (query != null)
                ? domainRepository.findByNameContainingIgnoreCase(query, pageable)
                : domainRepository.findAll(pageable);
        return domains.map(domainMapper::toResponse);
    }

    // READ (Get by ID)
    @Override
    @Transactional(readOnly = true)
    public DomainResponse getDomainById(Integer id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.NOT_FOUND));
        return domainMapper.toResponse(domain);
    }

    // UPDATE
    @Override
    public DomainResponse updateDomain(Integer id, DomainRequest request) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOMAIN_NOT_FOUND));

        domainMapper.updateDomain(domain, request);
        return domainMapper.toResponse(domainRepository.save(domain));
    }

    // DELETE
    @Override
    public void deleteDomain(Integer id) {
        Domain domain = domainRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOMAIN_NOT_FOUND));
        if (!domain.getJobs().isEmpty()) {
            throw new AppException(ErrorCode.CANT_DELETE_DOMAIN_IN_USE);
        }
        domainRepository.delete(domain);
    }
}
