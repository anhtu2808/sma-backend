package com.sma.core.service.impl;

import com.sma.core.dto.response.criteria.CriteriaResponse;
import com.sma.core.entity.Criteria;
import com.sma.core.mapper.criteria.CriteriaMapper;
import com.sma.core.repository.CriteriaRepository;
import com.sma.core.service.CriteriaService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class CriteriaServiceImpl implements CriteriaService {

    CriteriaRepository criteriaRepository;
    CriteriaMapper criteriaMapper;

    @Override
    public List<CriteriaResponse> getAllCriteria() {
        List<Criteria> criteriaList = criteriaRepository.findAll();
        return criteriaList.stream().map(criteriaMapper::toCriteriaResponse).toList();
    }
}
