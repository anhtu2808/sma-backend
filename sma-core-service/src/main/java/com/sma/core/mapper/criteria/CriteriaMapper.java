package com.sma.core.mapper.criteria;

import com.sma.core.dto.response.criteria.CriteriaResponse;
import com.sma.core.entity.Criteria;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CriteriaMapper {

    CriteriaResponse toCriteriaResponse(Criteria criteria);

}
