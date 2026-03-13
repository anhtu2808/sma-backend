package com.sma.core.service.impl;

import com.sma.core.dto.request.criteria.CriteriaRequest;
import com.sma.core.dto.response.PagingResponse;
import com.sma.core.dto.response.criteria.CriteriaResponse;
import com.sma.core.entity.Company;
import com.sma.core.entity.Criteria;
import com.sma.core.entity.Recruiter;
import com.sma.core.enums.Role;
import com.sma.core.exception.AppException;
import com.sma.core.exception.ErrorCode;
import com.sma.core.mapper.criteria.CriteriaMapper;
import com.sma.core.repository.CriteriaRepository;
import com.sma.core.repository.RecruiterRepository;
import com.sma.core.service.CriteriaService;
import com.sma.core.utils.JwtTokenProvider;
import io.jsonwebtoken.Jwt;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CriteriaServiceImpl implements CriteriaService {

    CriteriaRepository criteriaRepository;
    CriteriaMapper criteriaMapper;
    RecruiterRepository recruiterRepository;

    @Override
    public PagingResponse<CriteriaResponse> getAll(String name, Pageable pageable) {
        Page<Criteria> pageData;
        if (Objects.equals(JwtTokenProvider.getCurrentRole(), Role.RECRUITER)){
            Recruiter recruiter = recruiterRepository.findById(JwtTokenProvider.getCurrentRecruiterId())
                    .orElseThrow(() -> new AppException(ErrorCode.RECRUITER_NOT_EXISTED));
            if (name != null && !name.trim().isEmpty()) {
                pageData = criteriaRepository.findByNameContainingIgnoreCaseAndCompanyIdAndActive(name,
                        recruiter.getCompany().getId(), true, pageable);
            } else {
                pageData = criteriaRepository.findByCompanyIdAndActive(recruiter.getCompany().getId(), true, pageable);
            }
        } else {
            if (name != null && !name.trim().isEmpty()) {
                pageData = criteriaRepository.findByNameContainingIgnoreCase(name, pageable);
            } else {
                pageData = criteriaRepository.findAll(pageable);
            }
        }


        return PagingResponse.<CriteriaResponse>builder()
                .pageNumber(pageData.getNumber() + 1)
                .totalPages(pageData.getTotalPages())
                .pageSize(pageData.getSize())
                .totalElements(pageData.getTotalElements())
                .content(pageData.getContent().stream().map(criteriaMapper::toCriteriaResponse).toList())
                .first(pageData.isFirst())
                .last(pageData.isLast())
                .build();
    }

    @Override
    public CriteriaResponse getById(Integer id) {
        Criteria criteria = criteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_NOT_EXISTED));
        return criteriaMapper.toCriteriaResponse(criteria);
    }

    @Override
    @Transactional
    public CriteriaResponse create(CriteriaRequest request) {
        Criteria criteria = criteriaMapper.toCriteria(request);
        return criteriaMapper.toCriteriaResponse(criteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public CriteriaResponse update(Integer id, CriteriaRequest request) {
        Criteria criteria = criteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_NOT_EXISTED));
        criteriaMapper.updateCriteriaFromRequest(criteria, request);
        return criteriaMapper.toCriteriaResponse(criteriaRepository.save(criteria));
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Criteria criteria = criteriaRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CRITERIA_NOT_EXISTED));
        criteria.setActive(false);
        criteriaRepository.save(criteria);
    }

    @Override
    public void initCriteria(Company company) {
        Set<Criteria> criteriaSet = criteriaRepository.findByIsDefault(true);
        Set<Criteria> newCriteriaSet = new HashSet<>();
        criteriaSet.forEach(criteria -> {
            Criteria newCriteria = Criteria.builder()
                    .company(company)
                    .rule(criteria.getRule())
                    .weight(criteria.getWeight())
                    .name(criteria.getName())
                    .isDefault(false)
                    .build();
            newCriteriaSet.add(newCriteria);
        });
        criteriaRepository.saveAll(newCriteriaSet);
    }
}
