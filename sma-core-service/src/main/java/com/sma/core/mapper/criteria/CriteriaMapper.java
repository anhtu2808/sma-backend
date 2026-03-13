package com.sma.core.mapper.criteria;

import com.sma.core.dto.request.criteria.CriteriaRequest;
import com.sma.core.dto.response.criteria.CriteriaResponse;
import com.sma.core.entity.Criteria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CriteriaMapper {

    CriteriaResponse toCriteriaResponse(Criteria criteria);

    Criteria toCriteria(CriteriaRequest request);

    void updateCriteriaFromRequest(@MappingTarget Criteria criteria, CriteriaRequest request);
}
