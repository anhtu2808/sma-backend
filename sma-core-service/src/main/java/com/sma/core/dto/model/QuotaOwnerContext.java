package com.sma.core.dto.model;

import com.sma.core.enums.Role;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class QuotaOwnerContext {
    Role role;
    Integer candidateId;
    Integer recruiterId;
    Integer companyId;
}
